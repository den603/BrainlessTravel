package net.togogo.travel.user.feign;

import net.togogo.travel.common.Result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 调用 travel-ai-service 星火大模型能力的 Feign 客户端
 *
 * 【背景】
 * 问答模块（QaController → QaServiceImpl）归属 travel-user-service，
 * 但它需要调用星火大模型生成题目与评价，而大模型封装（SparkService）
 * 归属 travel-ai-service。两者跨服务，因此用 OpenFeign 做服务间调用。
 *
 * 【路径说明】
 * ai-service 配置了 server.servlet.context-path=/api，
 * 因此这里的 path 必须带上 /api 前缀：/api/internal/spark/**。
 * 该前缀未在网关路由表中注册，属于**服务内部接口**，不对外暴露。
 */
@FeignClient(
        name = "travel-ai-service",
        path = "/api/internal/spark",
        configuration = FeignHeaderRelayInterceptor.class
)
public interface AiSparkFeignClient {

    /**
     * 生成题目：入参 prompt，返回星火大模型原始 JSON 文本
     */
    @PostMapping("/questions")
    Result<String> generateQuestions(@RequestBody Map<String, String> body);

    /**
     * 生成评价：入参 prompt，返回星火大模型原始 JSON 文本
     */
    @PostMapping("/evaluation")
    Result<String> generateEvaluation(@RequestBody Map<String, String> body);
}
