package net.togogo.springboot_travel.controller;

import jakarta.annotation.Resource;
import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.dto.ChatRequest;
import net.togogo.springboot_travel.entity.ChatHistory;
import net.togogo.springboot_travel.mapper.ChatHistoryMapper;
import net.togogo.springboot_travel.service.SparkService;
import net.togogo.springboot_travel.util.LoginInterceptor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 自由聊天控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private SparkService sparkService;

    @Resource
    private ChatHistoryMapper chatHistoryMapper;

    /**
     * AI 自由聊天接口（自动保存对话记录）
     */
    @PostMapping("/ask")
    public Result<String> ask(@RequestBody ChatRequest request) {
        try {
            List<ChatRequest.Message> messages = request.getMessages();
            if (messages == null || messages.isEmpty()) {
                return Result.fail("消息内容不能为空");
            }

            // 获取当前用户ID
            Long userId = LoginInterceptor.getCurrentUserId();

            // 获取最后一条用户消息（当前问题）
            String userContent = messages.get(messages.size() - 1).getContent();

            // 调用大模型
            String reply = sparkService.chat(messages);

            // ========== 【追加】异步保存对话记录（不影响响应速度）==========
            // 保存用户消息
            saveChat(userId, "user", userContent);
            // 保存 AI 回复
            saveChat(userId, "assistant", reply);

            return Result.success(reply);
        } catch (Exception e) {
            return Result.fail("AI回复失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的聊天历史
     * GET /api/chat/history
     */
    @GetMapping("/history")
    public Result<List<ChatHistory>> getHistory() {
        Long userId = LoginInterceptor.getCurrentUserId();
        List<ChatHistory> list = chatHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getUserId, userId)
                        .orderByDesc(ChatHistory::getCreateTime)
                        .last("LIMIT 100") // 限制返回最近100条
        );
        return Result.success(list);
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
            System.err.println(">>> 【聊天存储失败】" + e.getMessage());
        }
    }
}