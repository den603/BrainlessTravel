package net.togogo.travel.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG 检索增强生成配置属性
 *
 * 【C盘防护关键点】
 * modelPath、docStoragePath 默认值全部指向 D 盘。
 * 所有文件读写必须使用本类中的配置值，**禁止硬编码 C 盘路径**。
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /** 是否启用 RAG 增强（false 时 /chat/ask/rag 退化为普通问答） */
    private boolean enabled = true;

    /**
     * 向量存储类型：
     * - memory：内存向量库（默认，开箱即用，重启丢失向量，仅用于开发测试）
     * - redis ：Redis 向量存储（**需要 Redis Stack**，普通 Redis 不支持向量检索）
     */
    private String storeType = "memory";

    /** BGE 中文嵌入模型 ONNX 文件所在目录（D 盘，约 100MB） */
    private String modelPath = "D:\\dev-resources\\rag-models";

    /** RAG 知识库上传文档的本地存储目录（D 盘） */
    private String docStoragePath = "D:\\dev-resources\\rag-docs";

    /** 文本切分参数 */
    private Splitter splitter = new Splitter();

    /** 检索参数 */
    private Retrieval retrieval = new Retrieval();

    /** Redis 向量存储参数 */
    private RedisConfig redis = new RedisConfig();

    @Data
    public static class Splitter {
        /** 单个片段最大字符数 */
        private int chunkSize = 500;
        /** 相邻片段重叠字符数 */
        private int chunkOverlap = 50;
    }

    @Data
    public static class Retrieval {
        /** 检索返回的最大片段数 */
        private int topK = 4;
        /** 最低相似度阈值，低于该值的片段会被丢弃 */
        private double minScore = 0.3;
    }

    @Data
    public static class RedisConfig {
        /** Redis 向量索引名 */
        private String indexName = "rag_embeddings";
        /** 向量维度：BGE-small-zh-v1.5 固定 512 维，不可修改 */
        private int vectorDimension = 512;
    }
}
