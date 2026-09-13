package net.togogo.travel.ai.service;

import net.togogo.travel.ai.dto.ChatRequest;
import net.togogo.travel.ai.dto.RagChatResponse;
import net.togogo.travel.ai.dto.RagSource;

import java.util.List;

/**
 * RAG 检索增强生成服务接口
 */
public interface RagService {

    /**
     * 带知识库检索增强的 AI 对话
     *
     * @param messages 对话历史（最后一条为用户当前问题）
     * @param useRag   true=启用知识库增强；false=退化为普通问答
     * @return 含回答内容、是否启用RAG、引用来源列表的响应
     * @throws Exception 调用大模型失败时抛出
     */
    RagChatResponse chatWithRag(List<ChatRequest.Message> messages, boolean useRag) throws Exception;

    /**
     * 纯检索：不调用大模型，只返回与 query 最相关的知识库片段
     *
     * @param query 查询语句
     * @return 相关片段列表（按相似度倒序）
     */
    List<RagSource> retrieve(String query);
}
