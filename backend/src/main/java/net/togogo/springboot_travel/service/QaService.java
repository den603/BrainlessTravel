package net.togogo.springboot_travel.service;

import cn.hutool.json.JSONArray;
import net.togogo.springboot_travel.dto.GenerateQuestionRequest;
import net.togogo.springboot_travel.dto.SubmitAnswerRequest;
import net.togogo.springboot_travel.entity.UserAnswerRecord;

public interface QaService {
    // 生成题目
    JSONArray generateQuestions(GenerateQuestionRequest request) throws Exception;

    // 提交答案
    UserAnswerRecord submitAnswers(SubmitAnswerRequest request) throws Exception;
}