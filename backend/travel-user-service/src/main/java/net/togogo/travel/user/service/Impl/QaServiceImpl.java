package net.togogo.travel.user.service.Impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.user.dto.GenerateQuestionRequest;
import net.togogo.travel.user.dto.SubmitAnswerRequest;
import net.togogo.travel.user.entity.*;
import net.togogo.travel.user.mapper.*;
import net.togogo.travel.user.service.QaService;
import net.togogo.travel.user.service.SparkService;
import net.togogo.travel.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class QaServiceImpl implements QaService {
    @Autowired
    private SparkService sparkService;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserAnswerRecordMapper recordMapper;
    @Autowired
    private UserAnswerDetailMapper detailMapper;
    @Autowired
    private QuestionFallbackMapper fallbackMapper;

    @Override
    public JSONArray generateQuestions(GenerateQuestionRequest request) throws Exception {
        JSONArray questions;
        try {
            // 1. 构造Prompt调用AI
            String prompt = buildQuestionPrompt(request);
            JSONObject aiResult = sparkService.generateQuestions(prompt);
            questions = aiResult.getJSONArray("questions");
            // 2. 校验题目数量
            if (questions.size() != request.getQuestionCount()) {
                throw new RuntimeException("题目数量不匹配");
            }
        } catch (Exception e) {
            log.error("AI生成题目失败，使用兜底题库: {}", e.getMessage());
            // 3. 兜底逻辑
            questions = getFallbackQuestions(request);
        }

        // 4. 存入数据库并返回（选项随机排序）
        List<JSONObject> resultList = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            JSONObject q = questions.getJSONObject(i);
            // 保存题目
            Question question = new Question();
            question.setScenicName(request.getScenicName());
            question.setDifficulty(request.getDifficulty());
            question.setContent(q.getStr("content"));
            question.setCorrectAnswer(q.getStr("correctAnswer"));
            question.setAnalysis(q.getStr("analysis"));
            question.setCreateTime(LocalDateTime.now());
            question.setUpdateTime(LocalDateTime.now());
            question.setIsDelete(0);

            // 选项随机排序
            JSONArray options = q.getJSONArray("options");
            List<String> optionList = options.toList(String.class);
            Collections.shuffle(optionList);
            question.setOptions(JSONUtil.toJsonStr(optionList));
            questionMapper.insert(question);

            // 构造返回结果（含题目ID、正确答案）
            JSONObject resultQ = new JSONObject();
            resultQ.set("questionId", question.getId());
            resultQ.set("content", question.getContent());
            resultQ.set("options", optionList);
            resultQ.set("correctAnswer", question.getCorrectAnswer()); // 前端隐藏
            resultQ.set("analysis", question.getAnalysis());
            resultList.add(resultQ);
        }
        return JSONUtil.parseArray(resultList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAnswerRecord submitAnswers(SubmitAnswerRequest request) throws Exception {
        Long userId = UserContext.getUserId();
        int totalQuestions = request.getAnswers().size();
        int correctCount = 0;
        // 改为收集错题内容（而非ID），最多保留3道避免Prompt过长
        List<String> wrongQuestionContents = new ArrayList<>();
        List<SubmitAnswerRequest.AnswerItem> answers = request.getAnswers();

        // 1. 先校验答案并统计正确率
        List<UserAnswerDetail> detailsToSave = new ArrayList<>();
        for (SubmitAnswerRequest.AnswerItem item : answers) {
            Question question = questionMapper.selectById(item.getQuestionId());
            if (question == null) throw new RuntimeException("题目不存在");

            boolean isCorrect = question.getCorrectAnswer().equals(item.getUserAnswer());
            if (isCorrect) {
                correctCount++;
            } else {
                // 收集错题内容，最多存3道（防止Prompt过长）
                if (wrongQuestionContents.size() < 3) {
                    wrongQuestionContents.add(question.getContent());
                }
            }

            // 预构建详情对象
            UserAnswerDetail detail = new UserAnswerDetail();
            detail.setQuestionId(item.getQuestionId());
            detail.setUserAnswer(item.getUserAnswer());
            detail.setIsCorrect(isCorrect ? 1 : 0);
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            detail.setIsDelete(0);
            detailsToSave.add(detail);
        }

        // 2. 计算得分
        int score = (int) Math.round((double) correctCount / totalQuestions * 100);

        // 3. 生成评价（传递错题内容而非ID）
        JSONObject evaluation;
        try {
            String prompt = buildEvaluationPrompt(request.getScenicName(), score, totalQuestions, wrongQuestionContents);
            evaluation = sparkService.generateEvaluation(prompt);
        } catch (Exception e) {
            log.error("AI生成评价失败，使用默认评价: {}", e.getMessage());
            evaluation = getDefaultEvaluation(score);
        }

        // 4. 先保存答题记录
        UserAnswerRecord record = new UserAnswerRecord();
        record.setUserId(userId);
        record.setScenicName(request.getScenicName());
        record.setTotalQuestions(totalQuestions);
        record.setScore(score);
        record.setEvaluateName(evaluation.getStr("evaluateName"));
        record.setEvaluateDesc(evaluation.getStr("evaluateDesc"));
        record.setAnswerTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDelete(0);
        recordMapper.insert(record);

        // 5. 批量保存答题详情
        for (UserAnswerDetail detail : detailsToSave) {
            detail.setRecordId(record.getId());
            detailMapper.insert(detail);
        }

        return record;
    }




    // 构造题目Prompt
    private String buildQuestionPrompt(GenerateQuestionRequest req) {
        String exampleJson = """
        {
          "questions": [
            {
              "content": "故宫的建成年代是？",
              "options": ["唐朝", "宋朝", "明朝", "清朝"],
              "correctAnswer": "明朝",
              "analysis": "故宫始建于明朝永乐四年（1406年），建成于永乐十八年（1420年）。"
            }
          ]
        }
        """;
        return String.format(
                "【角色】你是一个专业的旅游景点题目生成器。\n" +
                        "【任务】根据用户需求生成严格符合JSON结构的单选题。\n" +
                        "【绝对禁令】禁止输出任何解释、Markdown标记，仅输出JSON。\n" +
                        "【用户需求】\n" +
                        "- 景点：%s\n" +
                        "- 题数：%d题\n" +
                        "- 选项数：%d个\n" +
                        "- 难度：%s\n" +
                        "- 题目覆盖：历史背景、文化意义（必选），建筑艺术、地理知识、名人轶事、民俗传说（可选）\n" +
                        "【JSON结构示例】\n" +
                        "%s\n" +
                        "【输出】仅输出一个JSON对象，从 { 开始，到 } 结束。",
                req.getScenicName(), req.getQuestionCount(), req.getOptionCount(), req.getDifficulty(), exampleJson
        );
    }

    // 构造评价Prompt
    /**
     * 重构后的评价Prompt：传递错题内容，明确禁止输出ID/序号
     */
    private String buildEvaluationPrompt(String scenicName, int score, int total, List<String> wrongQuestionContents) {
        // 构造错题描述
        String wrongDesc;
        if (wrongQuestionContents.isEmpty()) {
            wrongDesc = "所有题目全部答对，知识掌握非常全面";
        } else {
            wrongDesc = "错题示例：" + String.join("；", wrongQuestionContents);
        }

        return String.format(
                "【角色】你是一个专业的旅游知识测评评价师。\n" +
                        "【任务】根据用户答题情况生成精准、友好的评价。\n" +
                        "【绝对禁令（违反则重写）】\n" +
                        "1. 绝对禁止出现任何题目ID、题目序号、数字编号\n" +
                        "2. 绝对禁止直接复述错题原文\n" +
                        "3. 评价名称严格控制在2-8个字\n" +
                        "4. 评价描述控制在50-200字，语言流畅自然\n" +
                        "【要求】\n" +
                        "- 先肯定用户的表现，再指出薄弱知识维度（如历史背景、建筑艺术、民俗文化、地理知识等）\n" +
                        "- 给出1-2条具体、可执行的改进建议\n" +
                        "- 语气鼓励为主，避免生硬批评\n" +
                        "【用户答题情况】\n" +
                        "- 测评景点：%s\n" +
                        "- 最终得分：%d分（满分100分）\n" +
                        "- %s\n" +
                        "【输出格式】严格输出JSON，不要任何额外文字、解释、Markdown标记\n" +
                        "【正确示例】\n" +
                        "{\"evaluateName\":\"历史达人\",\"evaluateDesc\":\"你对故宫的历史背景掌握得非常好，得分%d分！建议可以多了解一些民俗传说和建筑细节方面的知识。\"}\n" +
                        "【输出】",
                scenicName, score, wrongDesc, score
        );
    }

    // 兜底题库查询
    private JSONArray getFallbackQuestions(GenerateQuestionRequest req) {
        LambdaQueryWrapper<QuestionFallback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionFallback::getScenicName, req.getScenicName())
                .eq(QuestionFallback::getDifficulty, req.getDifficulty())
                .eq(QuestionFallback::getQuestionCount, req.getQuestionCount())
                .eq(QuestionFallback::getOptionCount, req.getOptionCount())
                .eq(QuestionFallback::getIsDelete, 0)
                .orderByDesc(QuestionFallback::getSort)
                .last("LIMIT 1");
        QuestionFallback fallback = fallbackMapper.selectOne(wrapper);
        if (fallback == null) {
            // 无兜底时生成默认题目
            return generateDefaultQuestions(req);
        }
        return JSONUtil.parseArray(fallback.getQuestions());
    }

    // 默认题目生成（极端兜底）
    private JSONArray generateDefaultQuestions(GenerateQuestionRequest req) {
        JSONArray questions = new JSONArray();
        for (int i = 0; i < req.getQuestionCount(); i++) {
            JSONObject q = new JSONObject();
            q.set("content", req.getScenicName() + "的默认题目" + (i + 1));
            List<String> options = new ArrayList<>();
            for (int j = 0; j < req.getOptionCount(); j++) {
                options.add("选项" + (char) ('A' + j));
            }
            q.set("options", options);
            q.set("correctAnswer", options.get(0));
            q.set("analysis", "这是一道默认题目");
            questions.add(q);
        }
        return questions;
    }

    // 默认评价
    private JSONObject getDefaultEvaluation(int score) {
        JSONObject eval = new JSONObject();
        if (score >= 80) {
            eval.set("evaluateName", "知识达人");
            eval.set("evaluateDesc", "你对该景点的知识掌握得非常不错，继续保持！");
        } else if (score >= 60) {
            eval.set("evaluateName", "入门游客");
            eval.set("evaluateDesc", "你对该景点有一定了解，建议可以多查阅相关资料哦。");
        } else {
            eval.set("evaluateName", "新手探索者");
            eval.set("evaluateDesc", "没关系，多了解该景点的历史文化，下次会更好的！");
        }
        return eval;
    }
}