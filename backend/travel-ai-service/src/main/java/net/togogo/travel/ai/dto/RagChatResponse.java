package net.togogo.travel.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * RAG 聊天响应 DTO
 */
@Data
public class RagChatResponse {

    /** AI 回答内容 */
    private String answer;

    /** 本次回答是否使用了知识库增强 */
    private Boolean ragEnabled;

    /** 引用的知识库片段来源列表（未命中为空列表） */
    private List<RagSource> sources;
}
