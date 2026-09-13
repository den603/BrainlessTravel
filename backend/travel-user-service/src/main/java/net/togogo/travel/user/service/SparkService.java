package net.togogo.travel.user.service;

import cn.hutool.json.JSONObject;

/**
 * 讯飞星火大模型服务接口（travel-user-service 侧适配版）
 *
 * ============================================================================
 * 【为什么 user-service 里也有一个 SparkService】
 * 问答模块（QaServiceImpl）需要「生成的题目」和「生成的评价」，
 * 但大模型调用封装归属 travel-ai-service。为了让 QaServiceImpl 的业务代码
 * 一行都不用改（它仍然 @Autowired 一个名为 SparkService 的 Bean），
 * 这里保留同名接口，由 {@link net.togogo.travel.user.service.Impl.SparkServiceImpl}
 * 通过 OpenFeign 转发到 travel-ai-service 执行。
 *
 * 【与 ai-service 侧 SparkService 的区别】
 * ai-service 侧的 SparkService 是完整实现（含 generateTravelPlan / chat 等4个方法，
 * 直接调星火 HTTP 接口）；本接口**只声明 user-service 实际用到的2个方法**，
 * 不复制 PlanRequest / ChatRequest 等仅属于 AI 服务的 DTO，避免模型类跨服务扩散。
 * ============================================================================
 */
public interface SparkService {

    /**
     * 生成题目（透传 prompt 给 ai-service）
     *
     * @param prompt 已构造好的提示词
     * @return 星火大模型返回的 JSON 对象
     * @throws Exception 调用或解析失败时抛出
     */
    JSONObject generateQuestions(String prompt) throws Exception;

    /**
     * 生成评价（透传 prompt 给 ai-service）
     *
     * @param prompt 已构造好的提示词
     * @return 星火大模型返回的 JSON 对象
     * @throws Exception 调用或解析失败时抛出
     */
    JSONObject generateEvaluation(String prompt) throws Exception;
}
