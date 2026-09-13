package net.togogo.travel.user.controller;

import cn.hutool.json.JSONArray;
import net.togogo.travel.common.Result.Result;
import net.togogo.travel.user.dto.GenerateQuestionRequest;
import net.togogo.travel.user.dto.SubmitAnswerRequest;
import net.togogo.travel.user.entity.User;
import net.togogo.travel.user.entity.UserAnswerRecord;
import net.togogo.travel.user.service.QaService;
import net.togogo.travel.user.service.UserService;
import net.togogo.travel.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/qa")
public class QaController {
    @Autowired
    private QaService qaService;
    @Autowired
    private UserService userService;

    // 生成题目
    @PostMapping("/generate")
    public Result<JSONArray> generateQuestions(@Validated @RequestBody GenerateQuestionRequest request) throws Exception {
        JSONArray questions = qaService.generateQuestions(request);
        return Result.success(questions);
    }

    // 提交答案
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAnswers(@Validated @RequestBody SubmitAnswerRequest request) throws Exception {
        UserAnswerRecord record = qaService.submitAnswers(request);
        // 获取用户信息
        User user = userService.getById(UserContext.getUserId());
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("evaluateName", record.getEvaluateName());
        result.put("scenicName", record.getScenicName());
        result.put("evaluateDesc", record.getEvaluateDesc());
        result.put("score", record.getScore());
        result.put("answerer", user.getNickName() != null ? user.getNickName() : "匿名用户");
        result.put("answerTime", record.getAnswerTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return Result.success(result);
    }
}