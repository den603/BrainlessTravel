package net.togogo.travel.user.service.Impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.common.Result.Result;
import net.togogo.travel.user.feign.AiSparkFeignClient;
import net.togogo.travel.user.service.SparkService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 星火大模型服务实现（travel-user-service 侧 Feign 适配器）
 *
 * 【职责】把 QaServiceImpl 的调用通过 OpenFeign 转发到 travel-ai-service，
 * 再把 ai-service 返回的 JSON 文本还原成 Hutool JSONObject，
 * 使 QaServiceImpl 的原有业务代码完全不需要改动。
 *
 * 【异常约定】
 * ai-service 返回 code=0 或网络异常时抛出 RuntimeException，
 * QaServiceImpl 侧已有 try-catch 兜底逻辑（生成题目失败会走本地兜底题库），
 * 因此行为与单体时代一致。
 */
@Slf4j
@Service
public class SparkServiceImpl implements SparkService {

    @Resource
    private AiSparkFeignClient aiSparkFeignClient;

    @Override
    public JSONObject generateQuestions(String prompt) throws Exception {
        return callAiService("生成题目", prompt, true);
    }

    @Override
    public JSONObject generateEvaluation(String prompt) throws Exception {
        return callAiService("生成评价", prompt, false);
    }

    /**
     * 统一的远程调用与结果解析
     *
     * @param scene      场景名，仅用于日志
     * @param prompt     提示词
     * @param isQuestion true=生成题目接口，false=生成评价接口
     */
    private JSONObject callAiService(String scene, String prompt, boolean isQuestion) throws Exception {
        Map<String, String> body = new HashMap<>(2);
        body.put("prompt", prompt);

        Result<String> result = isQuestion
                ? aiSparkFeignClient.generateQuestions(body)
                : aiSparkFeignClient.generateEvaluation(body);

        if (result == null) {
            throw new RuntimeException("AI服务无响应（" + scene + "）");
        }
        if (result.getCode() != 1 || result.getData() == null) {
            throw new RuntimeException("AI服务调用失败（" + scene + "）：" + result.getMsg());
        }

        try {
            return JSONUtil.parseObj(result.getData());
        } catch (Exception e) {
            log.error(">>> 【AI服务】{}返回内容不是合法JSON：{}", scene, result.getData());
            throw new RuntimeException("AI服务返回内容解析失败（" + scene + "）", e);
        }
    }
}
