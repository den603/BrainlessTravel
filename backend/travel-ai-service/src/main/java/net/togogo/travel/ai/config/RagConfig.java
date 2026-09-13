package net.togogo.travel.ai.config;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.ai.store.InMemoryEmbeddingStoreHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * RAG 检索增强生成核心配置
 *
 * ==================================================================================
 * 【C盘防护关键点 —— 必读】
 * 1. 本类保证 rag.model-path（D:\dev-resources\rag-models）与
 *    rag.doc-storage-path（D:\dev-resources\rag-docs）两个目录在启动时被创建，
 *    所有模型文件与上传文档都落在 D 盘。
 * 2. @PostConstruct 中在**创建任何 Bean 之前**把 java.io.tmpdir 指到
 *    D:\dev-resources\temp，防止 onnxruntime 等原生库往 C 盘系统临时目录写文件。
 * 3. BGE 模型的实际落盘策略见 {@link #materializeModelFile} 的详细注释。
 * ==================================================================================
 *
 * 【LangChain4j 0.36.0 API 差异说明（已核实 jar 内字节码，非猜测）】
 * - BgeSmallZhV15EmbeddingModel 的**真实包名**是
 *   dev.langchain4j.model.embedding.onnx.bgesmallzhv15（不是 ...bge.small.zh），
 *   且**只有无参构造与 Executor 构造**，没有接收缓存目录的构造函数，
 *   文档 9.5.2 节提到的「构造时指定模型缓存目录」在 0.36.0 中不存在。
 * - 0.36.0 中**没有** langchain4j.embeddings.cache.dir 这个系统属性，
 *   该模型是从 jar 内以 getResourceAsStream 流式读取的，**不会解压到系统临时目录**，
 *   因此原文档担心的「模型解压到 C 盘临时目录」在 0.36.0 上并不成立。
 * - 为了让模型真正落到 D 盘（满足磁盘验收项），本类采取更可靠的等价做法：
 *   把 jar 内的 bge-small-zh-v1.5.onnx 与 bge-small-zh-v1.5-tokenizer.json
 *   两个资源一次性提取到 D:\dev-resources\rag-models，然后用通用的
 *   OnnxEmbeddingModel(Path, Path, PoolingMode) 从 D 盘加载。
 *   已通过字节码核对：BgeSmallZhV15EmbeddingModel 内部正是调用
 *   loadFromJar("bge-small-zh-v1.5.onnx", "bge-small-zh-v1.5-tokenizer.json", PoolingMode.CLS)，
 *   与 loadFromFileSystem 读同一对文件、同一个 PoolingMode，
 *   因此「从 D 盘加载」与「从 jar 加载」在数值上完全等价。
 * - 若提取失败（如磁盘不可写），自动回退到 BgeSmallZhV15EmbeddingModel() 无参构造，
 *   保证服务仍能正常启动（此时模型从 jar 流式读取，依然不占 C 盘）。
 * ==================================================================================
 */
@Slf4j
@Configuration
public class RagConfig {

    /** jar 内 ONNX 模型资源名（BGE 模型类硬编码的同一个名字） */
    private static final String ONNX_RESOURCE = "bge-small-zh-v1.5.onnx";

    /** jar 内分词器资源名 */
    private static final String TOKENIZER_RESOURCE = "bge-small-zh-v1.5-tokenizer.json";

    /** 落盘文件名（与资源名保持一致） */
    private static final String ONNX_FILE_NAME = ONNX_RESOURCE;
    private static final String TOKENIZER_FILE_NAME = TOKENIZER_RESOURCE;

    @Resource
    private RagProperties ragProperties;

    @Resource
    private InMemoryEmbeddingStoreHolder inMemoryEmbeddingStoreHolder;

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private Integer redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /**
     * 【C盘防护关键点】在创建任何 Bean 之前锁定临时目录与模型目录
     */
    @PostConstruct
    public void initCpuDiskSafeDirectories() throws IOException {
        // 1. JVM 临时目录 → D 盘（与 AiServiceApplication.main 中的设置形成双保险）
        Path tempDir = Paths.get("D:\\dev-resources\\temp");
        Files.createDirectories(tempDir);
        System.setProperty("java.io.tmpdir", tempDir.toString());

        // 2. 模型目录 → D 盘
        Path modelDir = Paths.get(ragProperties.getModelPath());
        Files.createDirectories(modelDir);

        // 3. 上传文档目录 → D 盘
        Path docDir = Paths.get(ragProperties.getDocStoragePath());
        Files.createDirectories(docDir);

        log.info(">>> 【RAG】D盘目录就绪：modelPath={}，docStoragePath={}，tmpdir={}",
                modelDir.toAbsolutePath(), docDir.toAbsolutePath(), tempDir.toAbsolutePath());
    }

    /**
     * 中文嵌入模型（BGE-small-zh-v1.5，512维，本地 ONNX，零 API 依赖）
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        Path modelDir = Paths.get(ragProperties.getModelPath());
        Path onnxPath = modelDir.resolve(ONNX_FILE_NAME);
        Path tokenizerPath = modelDir.resolve(TOKENIZER_FILE_NAME);

        try {
            materializeModelFile(ONNX_RESOURCE, onnxPath);
            materializeModelFile(TOKENIZER_RESOURCE, tokenizerPath);

            log.info(">>> 【RAG】从 D 盘加载 BGE 嵌入模型：{}", onnxPath.toAbsolutePath());
            // PoolingMode.CLS 与 BgeSmallZhV15EmbeddingModel 内部使用的一致（已核对字节码）
            return new OnnxEmbeddingModel(onnxPath, tokenizerPath, PoolingMode.CLS);
        } catch (Exception e) {
            log.warn(">>> 【RAG】从 D 盘加载模型失败，回退为从 jar 内流式加载（同样不占用 C 盘）。原因：{}",
                    e.getMessage());
            return new BgeSmallZhV15EmbeddingModel();
        }
    }

    /**
     * 文本切分器：按递归方式切分，chunkSize / chunkOverlap 来自 rag.splitter 配置
     */
    @Bean
    public DocumentSplitter documentSplitter() {
        int chunkSize = ragProperties.getSplitter().getChunkSize();
        int chunkOverlap = ragProperties.getSplitter().getChunkOverlap();
        log.info(">>> 【RAG】文本切分器：chunkSize={}，chunkOverlap={}", chunkSize, chunkOverlap);
        return DocumentSplitters.recursive(chunkSize, chunkOverlap);
    }

    /**
     * 向量存储：根据 rag.store-type 决定使用内存向量库还是 Redis 向量库
     *
     * 【重要提醒】
     * - memory 模式：向量仅存在 JVM 内存中，**重启后全部丢失**，需调用
     *   POST /api/rag/reindex 重建。仅用于开发测试，生产环境必须切换 Redis Stack。
     * - redis 模式：需要 **Redis Stack**（带 RediSearch 模块）。普通 Redis 5.x/6.x
     *   不支持向量检索，启动时会直接报错，此时请把 store-type 改回 memory。
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        if ("redis".equalsIgnoreCase(ragProperties.getStoreType())) {
            log.info(">>> 【RAG】向量存储使用 Redis 模式（需 Redis Stack）：{}:{}，index={}，dimension={}",
                    redisHost, redisPort,
                    ragProperties.getRedis().getIndexName(),
                    ragProperties.getRedis().getVectorDimension());
            try {
                return RedisEmbeddingStore.builder()
                        .host(redisHost)
                        .port(redisPort)
                        .password(redisPassword == null || redisPassword.isEmpty() ? null : redisPassword)
                        .indexName(ragProperties.getRedis().getIndexName())
                        .dimension(ragProperties.getRedis().getVectorDimension())
                        // 声明可被过滤/返回的元数据字段，缺失会导致检索时取不到来源信息
                        .metadataKeys(List.of("documentId", "title", "fileType", "chunkIndex"))
                        .build();
            } catch (Exception e) {
                // 普通 Redis 不支持向量检索时会走到这里，直接终止启动比静默降级更安全（避免误以为向量已持久化）
                throw new IllegalStateException(
                        "Redis 向量存储初始化失败。请确认 Redis 为 Redis Stack（含 RediSearch 模块），"
                                + "否则请将 rag.store-type 改回 memory。原始错误：" + e.getMessage(), e);
            }
        }

        log.info(">>> 【RAG】向量存储使用内存模式（memory）：重启后向量会丢失，需重新调用 /api/rag/reindex 重建");
        InMemoryEmbeddingStore<TextSegment> store = inMemoryEmbeddingStoreHolder.getStore();
        // 内存向量库在 Bean 创建时清空一次，保证与 holder 中持有的是同一个实例
        store.removeAll();
        return store;
    }

    /**
     * 把 jar 内的模型资源提取到 D 盘（已存在则跳过，避免每次启动重复写 100MB）
     *
     * @param resourceName jar 根目录下的资源名
     * @param target       目标文件路径（D 盘）
     */
    private void materializeModelFile(String resourceName, Path target) throws IOException {
        if (Files.exists(target) && Files.size(target) > 0) {
            return;
        }

        try (InputStream in = BgeSmallZhV15EmbeddingModel.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("jar 内未找到模型资源：" + resourceName);
            }
            // 先写临时文件再原子移动，避免中途失败留下半个残缺文件
            Path tmp = target.resolveSibling(target.getFileName() + ".downloading");
            try (OutputStream out = Files.newOutputStream(tmp)) {
                in.transferTo(out);
            }
            Files.move(tmp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info(">>> 【C盘防护】模型资源已提取到 D 盘：{}（{} 字节）", target.toAbsolutePath(), Files.size(target));
        }
    }
}
