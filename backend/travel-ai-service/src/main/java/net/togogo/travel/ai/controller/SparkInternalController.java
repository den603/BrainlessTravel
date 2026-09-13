package net.togogo.travel.ai.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.ai.service.SparkService;
import net.togogo.travel.common.Result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 星火大模型「服务内部」接口控制器
 *
 * ============================================================================
 * 【这是给谁用的】
 * travel-user-service 的问答模块（QaServiceImpl）需要调用星火大模型生成题目与评价，
 * 而大模型封装归属本服务。两者通过 OpenFeign 做服务间调用，
 * 对端客户端见：net.togogo.travel.user.feign.AiSparkFeignClient。
 *
 * 【为什么不走网关】
 * 本控制器路径为 /api/internal/spark/**，**没有**注册到网关路由表中，
 * 属于服务内部接口，不对外暴露；Feign 通过 Nacos 服务发现直连本服务。
 * 同时 ai-service 的 WebMvcConfig 已把 /internal/** 排除在 UserIdInterceptor 之外
 * （内部调用没有网关下发的 X-User-Id）。
 *
 * 【返回格式】
 * 为了让 user-service 侧无需依赖本服务的 JSON 模型类，
 * 这里统一把 Hutool JSONObject 序列化为**字符串**放入 Result.data，
 * 由对端用 JSONUtil.parseObj 还原。
 * ============================================================================
 */
@Slf4j
@RestController
@RequestMapping("/internal/spark")
public class SparkInternalController {

    @Resource
    private SparkService sparkService;

    /**
     * 生成题目
     * POST /api/internal/spark/questions  {"prompt":"..."}
     */
    @PostMapping("/questions")
    public Result<String> generateQuestions(@RequestBody Map<String, String> body) {
        try {
            String prompt = body == null ? null : body.get("prompt");
            if (prompt == null || prompt.trim().isEmpty()) {
                return Result.fail("prompt 不能为空");
            }
            JSONObject result = sparkService.generateQuestions(prompt);
            return Result.success(JSONUtil.toJsonStr(result));
        } catch (Exception e) {
            log.error(">>> 【内部接口】生成题目失败：{}", e.getMessage(), e);
            return Result.fail("生成题目失败：" + e.getMessage());
        }
    }

    /**
     * 生成评价
     * POST /api/internal/spark/evaluation  {"prompt":"..."}
     */
    @PostMapping("/evaluation")
    public Result<String> generateEvaluation(@RequestBody Map<String, String> body) {
        try {
            String prompt = body == null ? null : body.get("prompt");
            if (prompt == null || prompt.trim().isEmpty()) {
                return Result.fail("prompt 不能为空");
            }
            JSONObject result = sparkService.generateEvaluation(prompt);
            return Result.success(JSONUtil.toJsonStr(result));
        } catch (Exception e) {
            log.error(">>> 【内部接口】生成评价失败：{}", e.getMessage(), e);
            return Result.fail("生成评价失败：" + e.getMessage());
        }
    }
}
