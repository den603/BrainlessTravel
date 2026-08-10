package net.togogo.springboot_travel.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.togogo.springboot_travel.dto.PlanRequest;
import net.togogo.springboot_travel.service.SparkService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SparkServiceImpl implements SparkService {

    @Value("${spark.app-id}")
    private String appId;

    @Value("${spark.api-key}")
    private String apiKey;

    @Value("${spark.api-secret}")
    private String apiSecret;

    @Value("${spark.url}")
    private String sparkUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();


    @Override
    public JSONObject generateTravelPlan(String prompt, PlanRequest req) throws Exception { // 注意：这里参数加了 PlanRequest req，方便兜底使用
        log.info("================== 开始调用星火大模型 ==================");
        log.info("发送的 Prompt: {}", prompt);

        // 1. 构建请求体 (流式)
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "spark-x");
        requestBody.set("stream", true);

        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", prompt);
        messages.add(userMessage);
        requestBody.set("messages", messages);

        String authUrl = getAuthUrl(sparkUrl, apiKey, apiSecret);
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(authUrl).post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .build();

        // 2. 执行请求，读取 SSE 流
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("星火 API 调用失败，状态码：{}，错误：{}", response.code(), errorBody);
                throw new RuntimeException("AI服务调用失败，状态码：" + response.code() + "，详情：" + errorBody);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new RuntimeException("响应体为空");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            log.info("================== 开始读取流式响应 ==================");

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String jsonData = line.substring(5).trim();
                    if (jsonData.isEmpty() || "[DONE]".equals(jsonData)) continue;

                    try {
                        JSONObject chunk = JSONUtil.parseObj(jsonData);

                        // 检查是否有错误
                        if (chunk.containsKey("header")) {
                            JSONObject header = chunk.getJSONObject("header");
                            if (header.getInt("code") != 0) {
                                log.error("星火返回错误: code={}, message={}",
                                        header.getInt("code"), header.getStr("message"));
                                throw new RuntimeException("AI返回错误: " + header.getStr("message"));
                            }
                            if (header.getInt("status") == 2) {
                                log.info("流式响应结束");
                                break;
                            }
                        }

                        // 提取内容
                        String extractedContent = extractContentFromChunk(chunk);
                        if (extractedContent != null && !extractedContent.isEmpty()) {
                            contentBuilder.append(extractedContent);
                        }
                    } catch (Exception e) {
                        // 忽略解析失败的块，继续下一个
                        log.debug("解析数据块失败: {}", e.getMessage());
                    }
                }
            }

            String fullContent = contentBuilder.toString();
            log.info("================== AI返回原始内容 ==================");
            log.info("完整内容: \n{}", fullContent);
            log.info("内容长度: {}", fullContent.length());
            log.info("====================================================");

            if (fullContent.isEmpty()) {
                log.warn("AI 未返回任何内容，触发兜底机制");
                return getFallbackPlan(req.getDestination(), req.getDays());
            }

            // 提取 JSON
            String jsonStr = extractJson(fullContent);

            if (jsonStr == null || jsonStr.isEmpty()) {
                log.error("无法从返回内容中提取 JSON，触发兜底机制");
                return getFallbackPlan(req.getDestination(), req.getDays());
            }

            // 3. 解析并校验 JSON (核心修改部分)
            try {
                JSONObject result = JSONUtil.parseObj(jsonStr);

                // 【关键校验】检查是否包含必要字段 dailyPlans
                if (!result.containsKey("dailyPlans") || result.getJSONArray("dailyPlans").isEmpty()) {
                    log.error("JSON缺少dailyPlans字段或为空，触发兜底机制");
                    return getFallbackPlan(req.getDestination(), req.getDays());
                }

                // 【可选校验】检查 summary 和 totalBudget
                if (!result.containsKey("summary")) {
                    result.set("summary", "为您定制的" + req.getDestination() + "之旅");
                }
                if (!result.containsKey("totalBudget")) {
                    result.set("totalBudget", (req.getBudgetMin() + req.getBudgetMax()) / 2);
                }

                log.info("JSON解析校验通过，返回结果");
                return result;

            } catch (Exception e) {
                log.error("JSON 解析失败，提取的字符串：{}", jsonStr);
                log.warn("触发兜底机制，返回默认行程");
                return getFallbackPlan(req.getDestination(), req.getDays());
            }
        }
    }

    private JSONObject getFallbackPlan(String destination, Integer days) {
        JSONObject fallback = new JSONObject();
        fallback.set("summary", "为您生成的" + destination + days + "天默认行程");
        fallback.set("totalBudget", 0);

        JSONArray dailyPlans = new JSONArray();
        for (int i = 1; i <= days; i++) {
            JSONObject dayPlan = new JSONObject();
            dayPlan.set("day", i);
            dayPlan.set("date", "2026-01-0" + i);
            dayPlan.set("theme", "第" + i + "天行程（默认）");
            // 填充默认景点/餐饮/酒店等字段（参考Prompt示例结构）
            JSONArray attractions = new JSONArray();
            JSONObject attraction = new JSONObject();
            attraction.set("name", destination + "默认景点");
            attraction.set("time", "09:00");
            attraction.set("duration", "2小时");
            attraction.set("description", "默认景点介绍");
            attraction.set("ticket", 0);
            attractions.add(attraction);
            dayPlan.set("attractions", attractions);

            // 餐饮/酒店/交通等字段同理，保证结构完整
            JSONObject hotel = new JSONObject();
            hotel.set("name", i == days ? "无" : "默认酒店");
            hotel.set("nightCost", i == days ? 0 : 200);
            hotel.set("note", i == days ? "最后一天无需住宿" : "默认住宿");
            dayPlan.set("hotel", hotel);

            dailyPlans.add(dayPlan);
        }
        fallback.set("dailyPlans", dailyPlans);
        return fallback;
    }


    /**
     * 从数据块中提取内容（兼容 content 和 reasoning_content）
     */
    private String extractContentFromChunk(JSONObject chunk) {
        try {
            // 路径1: payload.choices.text[].delta.content
            if (chunk.containsKey("payload")) {
                JSONObject payload = chunk.getJSONObject("payload");
                if (payload.containsKey("choices")) {
                    JSONObject choices = payload.getJSONObject("choices");
                    if (choices.containsKey("text")) {
                        JSONArray textArray = choices.getJSONArray("text");
                        for (int i = 0; i < textArray.size(); i++) {
                            JSONObject item = textArray.getJSONObject(i);

                            // 先尝试 delta
                            if (item.containsKey("delta")) {
                                JSONObject delta = item.getJSONObject("delta");
                                String content = getContentFromJson(delta);
                                if (content != null) return content;
                            }

                            // 再尝试直接从 item 获取
                            String content = getContentFromJson(item);
                            if (content != null) return content;
                        }
                    }
                }
            }

            // 路径2: 直接从 choices 获取（兼容不同格式）
            if (chunk.containsKey("choices")) {
                JSONArray choices = chunk.getJSONArray("choices");
                for (int i = 0; i < choices.size(); i++) {
                    JSONObject choice = choices.getJSONObject(i);
                    if (choice.containsKey("delta")) {
                        String content = getContentFromJson(choice.getJSONObject("delta"));
                        if (content != null) return content;
                    }
                    if (choice.containsKey("message")) {
                        String content = getContentFromJson(choice.getJSONObject("message"));
                        if (content != null) return content;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取内容时发生异常: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 从JSON对象中获取内容（尝试多个字段名）
     */
    private String getContentFromJson(JSONObject json) {
        // 按优先级尝试不同的字段名
        String[] fieldNames = {"content", "reasoning_content", "text", "result"};
        for (String fieldName : fieldNames) {
            if (json.containsKey(fieldName)) {
                String content = json.getStr(fieldName);
                if (content != null && !content.trim().isEmpty()) {
                    log.debug("从字段 '{}' 提取到内容: {}", fieldName, content);
                    return content;
                }
            }
        }
        return null;
    }


    /**
     * 终极版 JSON 提取器：专门针对星火大模型
     * 策略：找到包含 "dailyPlans" 关键字的最大可能 JSON 串
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) return null;

        // 1. 基础清理
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "").trim();
        if (text.startsWith("\uFEFF")) text = text.substring(1);

        log.info("extractJson: 开始处理，总长度={}", text.length());

        // 【关键修改1】从后往前找最后一个 "dailyPlans"，避开AI思考文字中的干扰
        int keywordIndex = text.lastIndexOf("dailyPlans");
        if (keywordIndex == -1) {
            log.warn("extractJson: 未找到 dailyPlans 关键字，尝试通用提取");
            return superExtractJsonLogic(text);
        }

        // 【关键修改2】从 keywordIndex 向前找，找到第一个使得括号净层数为0的 '{'
        // 这意味着这个 '{' 是包含 dailyPlans 的对象的开始
        int bestStart = -1;

        for (int i = keywordIndex; i >= 0; i--) {
            if (text.charAt(i) == '{') {
                // 检查从这个位置到 keywordIndex 之间，'{' 和 '}' 的数量
                int openCount = 0;
                int closeCount = 0;
                for (int j = i; j < keywordIndex; j++) {
                    char c = text.charAt(j);
                    // 简单计数，不考虑字符串内的情况（粗略估计）
                    if (c == '{') openCount++;
                    else if (c == '}') closeCount++;
                }
                // 如果 '{' 比 '}' 多1个，说明这是包含 dailyPlans 的父对象的开始
                if (openCount - closeCount == 1) {
                    bestStart = i;
                    break; // 找到最靠近 dailyPlans 的那个 '{'
                }
            }
        }

        if (bestStart == -1) {
            log.warn("extractJson: 无法定位根对象起始，尝试通用提取");
            return superExtractJsonLogic(text);
        }

        // 【关键修改3】从 bestStart 开始严格匹配括号，找到对应的 '}'
        int braceCount = 0;
        int bestEnd = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = bestStart; i < text.length(); i++) {
            char c = text.charAt(i);

            if (escaped) { escaped = false; continue; }
            if (c == '\\') { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }

            if (!inString) {
                if (c == '{') braceCount++;
                else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        bestEnd = i;
                        break;
                    }
                }
            }
        }

        if (bestEnd != -1) {
            String finalJson = text.substring(bestStart, bestEnd + 1);
            log.info("extractJson: 成功提取JSON，长度={}", finalJson.length());

            // 验证
            try {
                JSONObject test = JSONUtil.parseObj(finalJson);
                if (test.containsKey("dailyPlans")) {
                    return finalJson;
                }
            } catch (Exception e) {
                log.warn("extractJson: 提取的JSON验证失败");
            }
        }

        return superExtractJsonLogic(text);
    }

    /**
     * 原extractJson的核心逻辑（降级使用）
     */
    private String superExtractJsonLogic(String text) {
        // 情况A：已经是纯 JSON
        if ((text.startsWith("{") && text.endsWith("}")) || (text.startsWith("[") && text.endsWith("]"))) {
            log.info("extractJson: 情况A - 纯JSON");
            return text;
        }

        // 情况B：Markdown 代码块（多种变体）
        String[] codeBlockPatterns = {
                "```json\\s*([\\s\\S]*?)\\s*```",
                "```\\s*([\\s\\S]*?)\\s*```",
                "`\\s*([\\s\\S]*?)\\s*`",
                "```json([\\s\\S]*?)```",
                "```([\\s\\S]*?)```"
        };

        for (String pattern : codeBlockPatterns) {
            java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = r.matcher(text);
            if (m.find()) {
                String candidate = m.group(1).trim();
                if ((candidate.startsWith("{") && candidate.endsWith("}")) ||
                        (candidate.startsWith("[") && candidate.endsWith("]"))) {
                    log.info("extractJson: 情况B - 代码块匹配成功, pattern={}", pattern);
                    return candidate;
                }
            }
        }

        // 情况C：查找第一个 { 和最后一个 }（智能括号匹配）
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace > firstBrace) {
            String candidate = text.substring(firstBrace, lastBrace + 1);
            if (isBracketBalanced(candidate)) {
                log.info("extractJson: 情况C - 括号匹配成功");
                return candidate;
            } else {
                log.warn("extractJson: 括号不平衡，尝试寻找最大平衡子串");
                String balanced = findLargestBalancedSubstring(candidate);
                if (balanced != null) {
                    return balanced;
                }
            }
        }

        // 情况D：暴力尝试 - 逐行查找
        String[] lines = text.split("\\n");
        StringBuilder jsonBuilder = new StringBuilder();
        boolean inJson = false;
        int braceCount = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            for (char c : trimmed.toCharArray()) {
                if (c == '{') {
                    if (!inJson) inJson = true;
                    braceCount++;
                }
                if (inJson) jsonBuilder.append(c);
                if (c == '}') {
                    braceCount--;
                    if (braceCount == 0 && inJson) {
                        String result = jsonBuilder.toString();
                        log.info("extractJson: 情况D - 逐行查找成功");
                        return result;
                    }
                }
            }
            if (inJson) jsonBuilder.append("\n");
        }

        log.error("extractJson: 所有方法都失败了");
        return null;
    }

    /**
     * 检查括号是否平衡
     */
    private boolean isBracketBalanced(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '{') count++;
            if (c == '}') count--;
        }
        return count == 0;
    }

    /**
     * 寻找最大的平衡括号子串
     */
    private String findLargestBalancedSubstring(String s) {
        int maxLen = 0;
        String result = null;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '{') continue;
            int count = 1;
            for (int j = i + 1; j < s.length(); j++) {
                if (s.charAt(j) == '{') count++;
                if (s.charAt(j) == '}') count--;
                if (count == 0) {
                    if (j - i + 1 > maxLen) {
                        maxLen = j - i + 1;
                        result = s.substring(i, j + 1);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 鉴权 URL 生成（保持不变）
     */
    private String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        String builder = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "POST " + url.getPath() + " HTTP/1.1";
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(spec);
        byte[] signData = mac.doFinal(builder.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signData);
        String authHeader = String.format(
                "api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", signature);
        String authorization = Base64.getEncoder().encodeToString(authHeader.getBytes(StandardCharsets.UTF_8));
        return hostUrl + "?authorization=" + authorization + "&date=" + date.replace(" ", "%20") + "&host=" + url.getHost();
    }


    // 新增：生成题目
    @Override
    public JSONObject generateQuestions(String prompt) throws Exception {
        log.info("================== 开始调用星火大模型生成题目 ==================");
        log.info("发送的 Prompt: {}", prompt);

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "spark-x");
        requestBody.set("stream", true);
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", prompt);
        messages.add(userMessage);
        requestBody.set("messages", messages);

        String authUrl = getAuthUrl(sparkUrl, apiKey, apiSecret);
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(authUrl).post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("星火 API 调用失败，状态码：{}，错误：{}", response.code(), errorBody);
                throw new RuntimeException("AI服务调用失败");
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new RuntimeException("响应体为空");

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String jsonData = line.substring(5).trim();
                    if (jsonData.isEmpty() || "[DONE]".equals(jsonData)) continue;
                    try {
                        JSONObject chunk = JSONUtil.parseObj(jsonData);
                        if (chunk.containsKey("header")) {
                            JSONObject header = chunk.getJSONObject("header");
                            if (header.getInt("code") != 0) throw new RuntimeException("AI返回错误");
                            if (header.getInt("status") == 2) break;
                        }
                        String extractedContent = extractContentFromChunk(chunk);
                        if (extractedContent != null) contentBuilder.append(extractedContent);
                    } catch (Exception e) {
                        log.debug("解析数据块失败: {}", e.getMessage());
                    }
                }
            }

            String fullContent = contentBuilder.toString();
            log.info("AI返回原始内容: {}", fullContent);
            if (fullContent.isEmpty()) throw new RuntimeException("AI未返回内容");

            String jsonStr = extractQuestionsJson(fullContent);
            if (jsonStr == null) throw new RuntimeException("无法提取JSON");

            JSONObject result = JSONUtil.parseObj(jsonStr);
            if (!result.containsKey("questions") || result.getJSONArray("questions").isEmpty()) {
                throw new RuntimeException("JSON缺少questions字段");
            }
            return result;
        }
    }

    // 新增：生成评价
    @Override
    public JSONObject generateEvaluation(String prompt) throws Exception {
        log.info("================== 开始调用星火大模型生成评价 ==================");
        log.info("发送的 Prompt: {}", prompt);

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "spark-x");
        requestBody.set("stream", true);
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", prompt);
        messages.add(userMessage);
        requestBody.set("messages", messages);

        String authUrl = getAuthUrl(sparkUrl, apiKey, apiSecret);
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(authUrl).post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("AI服务调用失败");
            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new RuntimeException("响应体为空");

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String jsonData = line.substring(5).trim();
                    if (jsonData.isEmpty() || "[DONE]".equals(jsonData)) continue;
                    try {
                        JSONObject chunk = JSONUtil.parseObj(jsonData);
                        if (chunk.containsKey("header")) {
                            JSONObject header = chunk.getJSONObject("header");
                            if (header.getInt("code") != 0) throw new RuntimeException("AI返回错误");
                            if (header.getInt("status") == 2) break;
                        }
                        String extractedContent = extractContentFromChunk(chunk);
                        if (extractedContent != null) contentBuilder.append(extractedContent);
                    } catch (Exception e) {
                        log.debug("解析数据块失败: {}", e.getMessage());
                    }
                }
            }

            String fullContent = contentBuilder.toString();
            log.info("AI返回原始内容: {}", fullContent);
            if (fullContent.isEmpty()) throw new RuntimeException("AI未返回内容");

            String jsonStr = extractEvaluationJson(fullContent);
            if (jsonStr == null) throw new RuntimeException("无法提取JSON");

            JSONObject result = JSONUtil.parseObj(jsonStr);
            if (!result.containsKey("evaluateName") || !result.containsKey("evaluateDesc")) {
                throw new RuntimeException("JSON缺少必要字段");
            }
            return result;
        }
    }

    // 提取题目JSON（简化版，参考原extractJson）
    private String extractQuestionsJson(String text) {
        int keywordIndex = text.lastIndexOf("questions");
        if (keywordIndex == -1) return superExtractJsonLogic(text);
        // 简化括号匹配逻辑，参考原代码
        return superExtractJsonLogic(text);
    }

    // 提取评价JSON
    private String extractEvaluationJson(String text) {
        return superExtractJsonLogic(text);
    }


    /**
     * 【新增】通用 AI 对话（自由聊天）
     *
     * 【与 generateTravelPlan 的区别】
     * 1. 直接透传 messages 数组，不做 Prompt 包装
     * 2. 返回纯文本字符串，不做 JSON 提取和校验
     * 3. 无兜底机制，失败直接抛异常
     */
    @Override
    public String chat(List<net.togogo.springboot_travel.dto.ChatRequest.Message> messages) throws Exception {
        log.info("================== 开始调用星火大模型（自由聊天）==================");
        log.info("对话轮数: {}", messages.size());

        // 1. 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "spark-x");
        requestBody.set("stream", true);

        // 转换 messages 格式
        JSONArray msgArray = new JSONArray();
        for (net.togogo.springboot_travel.dto.ChatRequest.Message msg : messages) {
            JSONObject m = new JSONObject();
            m.set("role", msg.getRole());
            m.set("content", msg.getContent());
            msgArray.add(m);
        }
        requestBody.set("messages", msgArray);

        String authUrl = getAuthUrl(sparkUrl, apiKey, apiSecret);
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder().url(authUrl).post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .build();

        // 2. 执行请求，读取 SSE 流
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("星火 API 调用失败，状态码：{}，错误：{}", response.code(), errorBody);
                throw new RuntimeException("AI服务调用失败");
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new RuntimeException("响应体为空");

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
            StringBuilder contentBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String jsonData = line.substring(5).trim();
                    if (jsonData.isEmpty() || "[DONE]".equals(jsonData)) continue;

                    try {
                        JSONObject chunk = JSONUtil.parseObj(jsonData);
                        if (chunk.containsKey("header")) {
                            JSONObject header = chunk.getJSONObject("header");
                            if (header.getInt("code") != 0) {
                                throw new RuntimeException("AI返回错误: " + header.getStr("message"));
                            }
                            if (header.getInt("status") == 2) break;
                        }
                        String extractedContent = extractContentFromChunk(chunk);
                        if (extractedContent != null) contentBuilder.append(extractedContent);
                    } catch (Exception e) {
                        log.debug("解析数据块失败: {}", e.getMessage());
                    }
                }
            }

            String fullContent = contentBuilder.toString();
            log.info("AI返回内容长度: {}", fullContent.length());
            if (fullContent.isEmpty()) throw new RuntimeException("AI未返回内容");

            return fullContent;
        }
    }
}