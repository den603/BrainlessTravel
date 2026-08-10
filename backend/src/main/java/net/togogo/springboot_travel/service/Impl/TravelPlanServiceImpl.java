package net.togogo.springboot_travel.service.Impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import net.togogo.springboot_travel.dto.PlanRequest;
import net.togogo.springboot_travel.entity.TravelPlan;
import net.togogo.springboot_travel.mapper.TravelPlanMapper;
import net.togogo.springboot_travel.service.SparkService;
import net.togogo.springboot_travel.service.TravelPlanService;
import net.togogo.springboot_travel.util.LoginInterceptor;
import net.togogo.springboot_travel.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 旅行计划业务服务实现类（集成 Redis）
 *
 * 【缓存架构 - Cache Aside 模式】
 * 读流程：先读缓存 → 缓存有则复用计划内容 → 为当前用户保存数据库记录 → 返回
 * 写流程：调用大模型生成 → 写入 Redis → 保存到数据库 → 返回
 *
 * 【缓存 Key 设计】
 * plan:cache:{md5}  → 基于目的地+天数+人数+同行类型+预算+偏好+出发地生成的唯一标识
 * 相同目的地+相似需求的用户复用同一份 AI 生成结果，减少大模型调用次数。
 */
@Service
public class TravelPlanServiceImpl extends ServiceImpl<TravelPlanMapper, TravelPlan> implements TravelPlanService {

    @Autowired
    private SparkService sparkService;

    /**
     * 注入 Redis 工具类，用于操作缓存
     */
    @Resource
    private RedisUtil redisUtil;

    // ========== 缓存 Key 常量（集中管理，防止拼写错误，方便维护）==========

    /** 旅行计划缓存键前缀，完整 key = plan:cache:{md5} */
    private static final String PLAN_CACHE_PREFIX = "plan:cache:";

    /** 缓存过期时间：7 天。旅行计划内容变化频率低，适合长期缓存 */
    private static final long CACHE_TIMEOUT = 7;

    /**
     * 根据表单请求生成旅行计划，并保存到数据库（集成 Redis 缓存）
     *
     * 【执行流程】
     * 1. 根据请求参数构建缓存 key（目的地+天数+人数+同行类型+预算+偏好+出发地）
     * 2. 查询 Redis，命中则复用计划内容，为当前用户保存数据库记录后返回
     * 3. 缓存未命中，调用星火大模型生成计划
     * 4. 将生成结果写入 Redis（7 天过期）
     * 5. 保存到数据库，返回结果
     */
    @Override
    public JSONObject generateAndSavePlan(PlanRequest request) throws Exception {
        // 步骤1：构建缓存 key
        String cacheKey = buildCacheKey(request);

        // 步骤2：尝试从 Redis 读取缓存
        Object cacheObj = redisUtil.get(cacheKey);
        if (cacheObj != null) {
            // 【缓存命中】复用已生成的计划内容，减少大模型调用
            System.out.println(">>> 【Redis 缓存命中】获取旅行计划，key=" + cacheKey);
            String jsonStr = (String) cacheObj;
            JSONObject planJson = JSONUtil.parseObj(jsonStr);

            // 为当前用户保存一条历史记录（内容复用，但归属当前用户）
            Long planId = savePlanEntity(request, planJson);
            planJson.set("planId", planId);
            return planJson;
        }

        // 【缓存未命中】步骤3：调用星火大模型生成计划
        System.out.println(">>> 【星火大模型】生成旅行计划，目的地=" + request.getDestination());
        String prompt = buildPrompt(request);
        JSONObject planJson = sparkService.generateTravelPlan(prompt, request);

        // 校验并补充必要字段
        if (!planJson.containsKey("summary")) {
            planJson.set("summary", "为您定制的" + request.getDestination() + "之旅");
        }
        if (!planJson.containsKey("totalBudget")) {
            planJson.set("totalBudget", (request.getBudgetMin() + request.getBudgetMax()) / 2);
        }

        // 步骤4：将生成结果写入 Redis（存 JSON 字符串，避免 Hutool JSONObject 序列化兼容问题）
        redisUtil.set(cacheKey, planJson.toString(), CACHE_TIMEOUT, TimeUnit.DAYS);
        System.out.println(">>> 【Redis】旅行计划已缓存，key=" + cacheKey);

        // 步骤5：保存到数据库并返回
        Long planId = savePlanEntity(request, planJson);
        planJson.set("planId", planId);
        return planJson;
    }

    /**
     * 构建计划缓存 key
     *
     * 【设计思路】
     * 基于目的地、出发地、天数、人数、同行类型、预算范围、偏好等关键参数生成 MD5 摘要。
     * 相同参数的请求会生成相同的 key，从而复用同一份 AI 生成结果。
     *
     * @param req 前端表单请求
     * @return Redis 缓存键，格式：plan:cache:{md5}
     */
    private String buildCacheKey(PlanRequest req) {
        String preferencesStr = req.getPreferences() != null
                ? JSONUtil.toJsonStr(req.getPreferences())
                : "[]";
        String rawKey = String.format("%s|%s|%d|%d|%s|%d|%d|%s",
                req.getDestination(),
                req.getDeparture() != null ? req.getDeparture() : "none",
                req.getDays(),
                req.getPeopleCount(),
                req.getPeopleType() != null ? req.getPeopleType() : "none",
                req.getBudgetMin(),
                req.getBudgetMax(),
                preferencesStr
        );
        return PLAN_CACHE_PREFIX + SecureUtil.md5(rawKey);
    }

    /**
     * 保存计划到数据库（复用缓存内容或新生成内容）
     *
     * @param request  前端请求
     * @param planJson AI 生成的计划内容
     * @return 保存后的数据库主键 ID
     */
    private Long savePlanEntity(PlanRequest request, JSONObject planJson) {
        TravelPlan entity = new TravelPlan();
        entity.setUserId(LoginInterceptor.getCurrentUserId());
        entity.setDeparture(request.getDeparture());
        entity.setDestination(request.getDestination());
        entity.setTravelDate(request.getTravelDate());
        entity.setDays(request.getDays());
        entity.setPeopleCount(request.getPeopleCount());
        entity.setPeopleType(request.getPeopleType());
        entity.setBudgetMin(request.getBudgetMin());
        entity.setBudgetMax(request.getBudgetMax());
        entity.setPreferences(JSONUtil.toJsonStr(request.getPreferences()));
        entity.setPlanContent(planJson.toString());
        if (planJson.containsKey("totalBudget")) {
            entity.setTotalBudget(planJson.getBigDecimal("totalBudget"));
        }
        this.save(entity);
        return entity.getId();
    }

    /**
     * 构建发送给大模型的 Prompt
     */
    private String buildPrompt(PlanRequest req) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String travelDate = req.getTravelDate() != null ? req.getTravelDate().format(fmt) : "未指定";

        String exampleJson = """
        {
          "summary": "这是一个示例概述",
          "totalBudget": 3500,
          "dailyPlans": [
            {
              "day": 1,
              "date": "2026-XX-XX",
              "theme": "第一天主题",
              "attractions": [{"name": "景点A", "time": "09:00", "duration": "2小时", "description": "介绍", "ticket": 50}],
              "meals": [{"type": "午餐", "restaurant": "餐厅X", "cuisine": "菜系", "avgCost": 80}],
              "hotel": {"name": "酒店名", "nightCost": 300, "note": "备注"},
              "transport": "交通建议",
              "tips": "小贴士"
            },
            {
              "day": 2,
              "date": "2026-XX-XX",
              "theme": "第二天主题",
              "attractions": [{"name": "景点B", "time": "10:00", "duration": "3小时", "description": "介绍", "ticket": 0}],
              "meals": [{"type": "午餐", "restaurant": "餐厅Y", "cuisine": "菜系", "avgCost": 90}],
              "hotel": {"name": "酒店名", "nightCost": 300, "note": "备注"},
              "transport": "交通建议",
              "tips": "小贴士"
            },
            {
              "day": 3,
              "date": "2026-XX-XX",
              "theme": "第三天主题",
              "attractions": [{"name": "景点C", "time": "09:00", "duration": "2小时", "description": "介绍", "ticket": 0}],
              "meals": [{"type": "午餐", "restaurant": "餐厅Z", "cuisine": "菜系", "avgCost": 70}],
              "hotel": {"name": "无", "nightCost": 0, "note": "最后一天无需住宿"},
              "transport": "交通建议",
              "tips": "小贴士"
            }
          ]
        }
        """;

        return String.format(
                "【角色】你是一个专业的JSON数据生成器。\n" +
                        "【任务】根据用户需求，生成一份严格符合下方【JSON结构】的数据。\n" +
                        "【绝对禁令】\n" +
                        "1. 禁止输出任何中文解释、Markdown标记、代码块。\n" +
                        "2. 禁止输出 '```json' 或 '```'。\n" +
                        "3. 禁止将多天的数据合并到一个 'day' 对象中。\n" +
                        "4. 禁止任何字段为 null，不需要住宿时 hotel 必须填 {\"name\": \"无\", \"nightCost\": 0, \"note\": \"最后一天无需住宿\"}。\n" +
                        "5. 【最重要】禁止输出多个JSON对象！只能输出一个JSON对象，从 { 开始，到 } 结束。\n\n" +
                        "6. 【最重要】不要在JSON前输出任何文字说明、思考过程、分析内容，直接输出JSON！\n" +
                        "7. 【最重要】JSON 必须是一行开始的第一个字符，前面不能有换行或空格以外的内容！\n" +
                        "【用户需求】\n" +
                        "- 出发地：%s\n" +
                        "- 目的地：%s\n" +
                        "- 出行日期：%s\n" +
                        "- 天数：%d天\n" +
                        "- 同行：%d人（%s）\n" +
                        "- 预算：%d-%d元\n" +
                        "- 偏好：%s\n\n" +
                        "【JSON结构示例】请严格按照以下示例的层级生成，不要改变字段名：\n" +
                        "%s\n\n" +
                        "【输出】仅输出一个JSON字符串，从 { 开始，到 } 结束。",
                req.getDeparture() != null ? req.getDeparture() : "未指定",
                req.getDestination(),
                travelDate,
                req.getDays(),
                req.getPeopleCount(),
                req.getPeopleType() != null ? req.getPeopleType() : "朋友",
                req.getBudgetMin(),
                req.getBudgetMax(),
                req.getPreferences() != null ? String.join("、", req.getPreferences()) : "无特殊偏好",
                exampleJson
        );
    }

    @Override
    public List<TravelPlan> getCurrentUserPlans() {
        Long userId = LoginInterceptor.getCurrentUserId();
        return this.list(new LambdaQueryWrapper<TravelPlan>()
                .eq(TravelPlan::getUserId, userId)
                .orderByDesc(TravelPlan::getCreatedAt));
    }

    /**
     * 带分页和搜索的查询实现
     */
    @Override
    public Page<TravelPlan> searchCurrentUserPlans(String keyword, int current, int size) {
        Long userId = LoginInterceptor.getCurrentUserId();
        Page<TravelPlan> page = new Page<>(current, size);

        LambdaQueryWrapper<TravelPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TravelPlan::getUserId, userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(TravelPlan::getDestination, keyword);
        }
        queryWrapper.orderByDesc(TravelPlan::getCreatedAt);

        return this.page(page, queryWrapper);
    }
}