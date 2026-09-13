package net.togogo.travel.ai.service.Impl;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.ai.config.RagProperties;
import net.togogo.travel.ai.dto.ChatRequest;
import net.togogo.travel.ai.dto.RagChatResponse;
import net.togogo.travel.ai.dto.RagSource;
import net.togogo.travel.ai.service.RagService;
import net.togogo.travel.ai.service.SparkService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RAG 检索增强生成服务实现
 *
 * 【核心流程】
 * 1. useRag=false 或 rag.enabled=false → 直接调星火，返回 ragEnabled=false
 * 2. 取最后一条用户消息作为 query → 向量化 → 在向量库中检索 topK 个最相关片段
 * 3. 命中片段时构造增强 System Prompt（含参考资料并标注 [1][2]）插入 messages 头部
 * 4. 调用星火大模型生成回答
 * 5. 从片段 metadata 中提取 documentId / title / fileType / chunkIndex 构造引用来源
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    /** 片段摘要最大长度（超出部分截断） */
    private static final int SUMMARY_LENGTH = 200;

    /** metadata 中的键名常量 */
    private static final String META_DOCUMENT_ID = "documentId";
    private static final String META_TITLE = "title";
    private static final String META_FILE_TYPE = "fileType";
    private static final String META_CHUNK_INDEX = "chunkIndex";

    @Resource
    private RagProperties ragProperties;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private SparkService sparkService;

    @Override
    public RagChatResponse chatWithRag(List<ChatRequest.Message> messages, boolean useRag) throws Exception {
        RagChatResponse response = new RagChatResponse();

        // ========== 1. 未开启RAG 或 全局未启用 → 退化为普通问答 ==========
        if (!useRag || !ragProperties.isEnabled()) {
            response.setAnswer(sparkService.chat(messages));
            response.setRagEnabled(false);
            response.setSources(Collections.emptyList());
            return response;
        }

        // ========== 2. 取最后一条用户消息作为检索 query ==========
        String query = extractLastUserContent(messages);
        List<RagSource> sources = Collections.emptyList();

        if (query != null && !query.trim().isEmpty()) {
            sources = retrieveInternal(query);
        }

        // ========== 3. 未命中任何片段 → 仍然走普通问答，但告知前端 RAG 已开启 ==========
        if (sources.isEmpty()) {
            log.info(">>> 【RAG】未检索到相关知识片段，退化为普通问答。query={}", query);
            response.setAnswer(sparkService.chat(messages));
            response.setRagEnabled(true);
            response.setSources(Collections.emptyList());
            return response;
        }

        // ========== 4. 构造增强消息：System Prompt 插入头部 ==========
        List<ChatRequest.Message> enhancedMessages = new ArrayList<>(messages.size() + 1);
        enhancedMessages.add(buildSystemMessage(sources));
        enhancedMessages.addAll(messages);

        log.info(">>> 【RAG】命中 {} 个知识片段，已构造增强提示词。query={}", sources.size(), query);
        response.setAnswer(sparkService.chat(enhancedMessages));
        response.setRagEnabled(true);
        response.setSources(sources);
        return response;
    }

    @Override
    public List<RagSource> retrieve(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return retrieveInternal(query);
    }

    /**
     * 向量化 + 相似度检索，并转换为 RagSource 列表
     */
    private List<RagSource> retrieveInternal(String query) {
        try {
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                    queryEmbedding,
                    ragProperties.getRetrieval().getTopK(),
                    ragProperties.getRetrieval().getMinScore()
            );

            List<RagSource> sources = new ArrayList<>(matches.size());
            for (EmbeddingMatch<TextSegment> match : matches) {
                TextSegment segment = match.embedded();
                if (segment == null) {
                    continue;
                }
                RagSource source = new RagSource();
                Metadata metadata = segment.metadata();
                source.setDocumentId(getMetaLong(metadata, META_DOCUMENT_ID));
                source.setTitle(getMetaString(metadata, META_TITLE));
                source.setFileType(getMetaString(metadata, META_FILE_TYPE));
                source.setChunkIndex(getMetaInteger(metadata, META_CHUNK_INDEX));
                source.setContent(truncate(segment.text(), SUMMARY_LENGTH));
                source.setScore(match.score());
                sources.add(source);
            }
            return sources;
        } catch (Exception e) {
            // 检索失败不应中断对话：返回空来源，让上层退化为普通问答
            log.error(">>> 【RAG】向量检索失败，降级为普通问答：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 构造增强 System Prompt，把检索到的片段编号为 [1][2]... 交给大模型
     */
    private ChatRequest.Message buildSystemMessage(List<RagSource> sources) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的旅游规划助手。请**优先根据下面提供的参考资料**回答用户问题。\n");
        sb.append("要求：\n");
        sb.append("1. 回答中引用参考资料时，请用 [1]、[2] 这样的编号标注来源；\n");
        sb.append("2. 如果参考资料不足以回答问题，请如实说明，并可结合常识补充；\n");
        sb.append("3. 不要编造参考资料中不存在的信息。\n\n");
        sb.append("【参考资料】\n");

        for (int i = 0; i < sources.size(); i++) {
            RagSource source = sources.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append(source.getTitle() == null ? "未命名文档" : source.getTitle())
                    .append("：")
                    .append(source.getContent() == null ? "" : source.getContent())
                    .append("\n");
        }

        ChatRequest.Message systemMessage = new ChatRequest.Message();
        systemMessage.setRole("system");
        systemMessage.setContent(sb.toString());
        return systemMessage;
    }

    /**
     * 取对话历史中最后一条 role=user 的消息内容
     */
    private String extractLastUserContent(List<ChatRequest.Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatRequest.Message message = messages.get(i);
            if (message != null && "user".equalsIgnoreCase(message.getRole())) {
                return message.getContent();
            }
        }
        // 没有 role=user 标记时退化为取最后一条
        return messages.get(messages.size() - 1).getContent();
    }

    /**
     * 安全读取字符串型 metadata
     */
    private String getMetaString(Metadata metadata, String key) {
        if (metadata == null || !metadata.containsKey(key)) {
            return null;
        }
        try {
            return metadata.getString(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全读取 Long 型 metadata（向量库序列化后可能变成 Double/String，需要兼容）
     */
    private Long getMetaLong(Metadata metadata, String key) {
        String raw = getMetaString(metadata, key);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            try {
                return (long) Double.parseDouble(raw.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    /**
     * 安全读取 Integer 型 metadata
     */
    private Integer getMetaInteger(Metadata metadata, String key) {
        Long value = getMetaLong(metadata, key);
        return value == null ? null : value.intValue();
    }

    /**
     * 截断过长的片段文本，避免响应体膨胀
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }
}
