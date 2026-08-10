package net.togogo.springboot_travel.controller;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.dto.PlanRequest;
import net.togogo.springboot_travel.entity.TravelPlan;
import net.togogo.springboot_travel.service.TravelPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 旅行计划接口控制器
 * 提供生成计划、查看历史、查看详情的 RESTful 接口
 */
@RestController
@RequestMapping("/plan")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private ObjectMapper objectMapper; // 注入Spring自带的Jackson序列化器

    /**
     * 生成旅行计划
     * POST /api/plan/generate
     */
    @PostMapping("/generate")
    public Result<JsonNode> generatePlan(@RequestBody PlanRequest request) {
        try {
            JSONObject plan = travelPlanService.generateAndSavePlan(request);

            // 关键修复：将Hutool JSONObject转换为Jackson JsonNode
            // 解决Hutool JSONNull无法被Jackson序列化的问题
            String jsonStr = plan.toString();
            JsonNode jsonNode = objectMapper.readTree(jsonStr);

            return Result.success(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("生成失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户的历史计划列表
     * GET /api/plan/history
     */
    @GetMapping("/history")
    public Result<List<TravelPlan>> getHistory() {
        List<TravelPlan> plans = travelPlanService.getCurrentUserPlans();
        return Result.success(plans);
    }

    /**
     * 查看计划详情
     * GET /api/plan/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<JsonNode> getDetail(@PathVariable Long id) {
        try {
            TravelPlan plan = travelPlanService.getById(id);
            if (plan == null) {
                return Result.fail("计划不存在");
            }
            // 同样转换为Jackson JsonNode
            JsonNode jsonNode = objectMapper.readTree(plan.getPlanContent());
            return Result.success(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("解析失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户的历史计划列表 (支持搜索和分页)
     * GET /api/plan/history?keyword=广州&page=1&size=10
     */
    @GetMapping("/history/search")
    public Result<Page<TravelPlan>> searchHistory(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TravelPlan> resultPage = travelPlanService.searchCurrentUserPlans(keyword, page, size);
        return Result.success(resultPage);
    }
}