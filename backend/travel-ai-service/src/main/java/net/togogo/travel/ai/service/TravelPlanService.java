package net.togogo.travel.ai.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import net.togogo.travel.ai.dto.PlanRequest;
import net.togogo.travel.ai.entity.TravelPlan;

import java.util.List;

/**
 * 旅行计划业务服务接口
 * 继承 MyBatis-Plus IService，增加自定义方法
 */
public interface TravelPlanService extends IService<TravelPlan> {

    /**
     * 根据表单请求生成旅行计划，并保存到数据库
     * @param request 前端表单数据
     * @return AI 生成的计划 JSON，已包含 planId
     * @throws Exception 生成失败时抛出
     */
    JSONObject generateAndSavePlan(PlanRequest request) throws Exception;

    /**
     * 获取当前登录用户的所有历史计划（按创建时间倒序）
     * @return 计划实体列表
     */
    List<TravelPlan> getCurrentUserPlans();

    // 【新增】带分页和关键词搜索的历史记录查询
    Page<TravelPlan> searchCurrentUserPlans(String keyword, int current, int size);
}