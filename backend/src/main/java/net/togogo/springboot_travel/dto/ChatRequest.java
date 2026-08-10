package net.togogo.springboot_travel.dto;

import lombok.Data;
import java.util.List;

/**
 * AI 自由聊天请求 DTO
 *
 * 【字段说明】
 * messages：对话历史数组，格式与星火大模型 API 一致
 * 示例：[{"role":"user","content":"你好"},{"role":"assistant","content":"你好"},{"role":"user","content":"推荐北京景点"}]
 */
@Data
public class ChatRequest {

    /**
     * 对话消息列表
     */
    private List<Message> messages;

    /**
     * 单条消息内部类
     */
    @Data
    public static class Message {
        private String role;     // user 或 assistant
        private String content;  // 消息内容
    }
}