package net.togogo.travel.ai.service;

import cn.hutool.json.JSONObject;
import net.togogo.travel.ai.dto.ChatRequest;
import net.togogo.travel.ai.dto.PlanRequest;

import java.util.List;

/**
 * 讯飞星火大模型服务接口
 * 提供同步调用大模型并返回结构化 JSON 的能力
 */
public interface SparkService {

    /**
     * 根据提示词生成旅行计划
     * @param prompt 构造好的提示词文本
     * @return AI 返回的计划 JSON 对象
     * @throws Exception 调用失败或解析失败时抛出
     */
    JSONObject generateTravelPlan(String prompt, PlanRequest req) throws Exception;

    // 新增：生成题目
    JSONObject generateQuestions(String prompt) throws Exception;

    // 新增：生成评价
    JSONObject generateEvaluation(String prompt) throws Exception;
    /**
     * 【新增】通用 AI 对话（自由聊天）
     *
     * @param messages 对话历史（包含当前问题）
     * @return AI 回复的纯文本
     * @throws Exception 调用失败时抛出
     */
    String chat(List<ChatRequest.Message> messages) throws Exception;
}