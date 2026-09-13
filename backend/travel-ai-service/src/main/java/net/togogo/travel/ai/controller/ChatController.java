package net.togogo.travel.ai.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.ai.dto.ChatRequest;
import net.togogo.travel.ai.dto.RagChatResponse;
import net.togogo.travel.ai.entity.ChatHistory;
import net.togogo.travel.ai.mapper.ChatHistoryMapper;
import net.togogo.travel.ai.service.RagService;
import net.togogo.travel.ai.service.SparkService;
import net.togogo.travel.common.Result.Result;
import net.togogo.travel.common.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 自由聊天控制器
 *
 * 【微服务改造说明】
 * 1. 原有 /chat/ask 与 /chat/history 接口行为完全不变；
 * 2. 新增 /chat/ask/rag 接口，在原有自由聊天基础上叠加 RAG 知识库检索增强，
 *    返回内容除回答外还包含引用来源；
 * 3. 用户ID统一改为从 UserContext 获取（由网关下发的 X-User-Id 填充），
 *    不再使用原 LoginInterceptor.getCurrentUserId()。
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    /** 线程安全的 JSON 转换器（ObjectMapper 设计上即线程安全，静态复用即可） */
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @Resource
    private SparkService sparkService;

    @Resource
    private ChatHistoryMapper chatHistoryMapper;

    /** 【新增】RAG 检索增强服务 */
    @Resource
    private RagService ragService;

    /**
     * AI 自由聊天接口（自动保存对话记录）—— 原有接口，行为不变
     */
    @PostMapping("/ask")
    public Result<String> ask(@RequestBody ChatRequest request) {
        try {
            List<ChatRequest.Message> messages = request.getMessages();
            if (messages == null || messages.isEmpty()) {
                return Result.fail("消息内容不能为空");
            }

            // 获取当前用户ID（由网关通过 X-User-Id 下发）
            Long userId = UserContext.getUserId();

            // 获取最后一条用户消息（当前问题）
            String userContent = messages.get(messages.size() - 1).getContent();

            // 调用大模型
            String reply = sparkService.chat(messages);

            // ========== 异步保存对话记录（不影响响应速度）==========
            // 保存用户消息
            saveChat(userId, "user", userContent);
            // 保存 AI 回复
            saveChat(userId, "assistant", reply);

            return Result.success(reply);
        } catch (Exception e) {
            log.error(">>> 【AI聊天】调用失败：{}", e.getMessage(), e);
            return Result.fail("AI回复失败：" + e.getMessage());
        }
    }

    /**
     * 【新增】带知识库检索增强的 AI 聊天接口
     *
     * POST /api/chat/ask/rag
     * 请求体：{"messages":[{"role":"user","content":"故宫门票多少钱"}],"useRag":true}
     * 响应：Result<RagChatResponse>{ answer, ragEnabled, sources[] }
     *
     * - useRag=true ：先检索旅游知识库，命中则把参考资料拼进 System Prompt 再问大模型
     * - useRag=false：退化为普通问答，sources 为空
     */
    @PostMapping("/ask/rag")
    public Result<RagChatResponse> askWithRag(@RequestBody Map<String, Object> request) {
        try {
            // 1. 解析 messages（复用原有 ChatRequest 结构，保证与 /chat/ask 完全一致）
            Object rawMessages = request.get("messages");
            if (rawMessages == null) {
                return Result.fail("消息内容不能为空");
            }
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setMessages(convertMessages(rawMessages));
            List<ChatRequest.Message> messages = chatRequest.getMessages();
            if (messages.isEmpty()) {
                return Result.fail("消息内容不能为空");
            }

            // 2. 解析 useRag 开关，默认开启
            boolean useRag = parseUseRag(request.get("useRag"));

            Long userId = UserContext.getUserId();

            // 3. 调用 RAG 服务
            RagChatResponse response = ragService.chatWithRag(messages, useRag);

            // 4. 保存对话记录（与 /chat/ask 行为保持一致）
            String userContent = messages.get(messages.size() - 1).getContent();
            saveChat(userId, "user", userContent);
            saveChat(userId, "assistant", response.getAnswer());

            return Result.success(response);
        } catch (Exception e) {
            log.error(">>> 【AI聊天】RAG 问答失败：{}", e.getMessage(), e);
            return Result.fail("AI回复失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的聊天历史
     * GET /api/chat/history
     */
    @GetMapping("/history")
    public Result<List<ChatHistory>> getHistory() {
        Long userId = UserContext.getUserId();
        List<ChatHistory> list = chatHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getUserId, userId)
                        .orderByDesc(ChatHistory::getCreateTime)
                        .last("LIMIT 100") // 限制返回最近100条
        );
        return Result.success(list);
    }

    /**
     * 把请求体中的 messages 数组转换为强类型列表（静态复用，避免每次请求新建 ObjectMapper）
     */
    private List<ChatRequest.Message> convertMessages(Object rawMessages) {
        return OBJECT_MAPPER.convertValue(rawMessages,
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, ChatRequest.Message.class));
    }

    /**
     * 解析 useRag 开关：缺省或格式非法时默认开启
     */
    private boolean parseUseRag(Object rawUseRag) {
        if (rawUseRag == null) {
            return true;
        }
        if (rawUseRag instanceof Boolean value) {
            return value;
        }
        return Boolean.parseBoolean(String.valueOf(rawUseRag));
    }

    /**
     * 保存单条聊天记录
     */
    private void saveChat(Long userId, String role, String content) {
        try {
            ChatHistory history = new ChatHistory();
            history.setUserId(userId);
            history.setRole(role);
            history.setContent(content);
            history.setSessionId("default");
            chatHistoryMapper.insert(history);
        } catch (Exception e) {
            // 存储失败不影响主流程，只打日志
            log.error(">>> 【聊天存储失败】{}", e.getMessage());
        }
    }
}
