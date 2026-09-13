package net.togogo.travel.user.service;

import cn.hutool.json.JSONArray;
import net.togogo.travel.user.dto.GenerateQuestionRequest;
import net.togogo.travel.user.dto.SubmitAnswerRequest;
import net.togogo.travel.user.entity.UserAnswerRecord;

public interface QaService {
    // 生成题目
    JSONArray generateQuestions(GenerateQuestionRequest request) throws Exception;

    // 提交答案
    UserAnswerRecord submitAnswers(SubmitAnswerRequest request) throws Exception;
}