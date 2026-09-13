package net.togogo.travel.ai.service.Impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.ai.config.RagProperties;
import net.togogo.travel.ai.entity.RagDocument;
import net.togogo.travel.ai.entity.ScenicSimple;
import net.togogo.travel.ai.mapper.RagDocumentMapper;
import net.togogo.travel.ai.mapper.ScenicSimpleMapper;
import net.togogo.travel.ai.service.RagDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * RAG 知识库文档管理服务实现
 *
 * 【C盘防护关键点】
 * 所有文件读写路径均取自 {@link RagProperties}（默认 D:\dev-resources\rag-docs），
 * **本类中不存在任何硬编码的 C 盘路径**。
 */
@Slf4j
@Service
public class RagDocumentServiceImpl implements RagDocumentService {

    /** 允许上传的扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "docx", "txt", "xlsx");

    /**
     * 允许的 MIME 类型。
     * 注意：微信小程序 uni.uploadFile 常把 Content-Type 传成 application/octet-stream，
     * 因此这里额外放行空值与 octet-stream，真正可靠的校验以**扩展名**为准。
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/octet-stream"
    );

    /** 单文件大小上限 10MB（与 spring.servlet.multipart.max-file-size 保持一致） */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    /** 来源类型 */
    private static final String SOURCE_USER = "USER";
    private static final String SOURCE_SYSTEM = "SYSTEM";
    private static final String SOURCE_SCENIC = "SCENIC";

    /** 处理状态 */
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    /** 系统预置数据的用户ID */
    private static final Long SYSTEM_USER_ID = 0L;

    /** metadata 键名（与 RagServiceImpl 中读取的键名必须完全一致） */
    private static final String META_DOCUMENT_ID = "documentId";
    private static final String META_TITLE = "title";
    private static final String META_FILE_TYPE = "fileType";
    private static final String META_CHUNK_INDEX = "chunkIndex";

    @Resource
    private RagProperties ragProperties;

    @Resource
    private RagDocumentMapper ragDocumentMapper;

    @Resource
    private ScenicSimpleMapper scenicSimpleMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private DocumentSplitter documentSplitter;

    /**
     * 自身的代理引用。
     * 【为什么需要】@Async 依赖 Spring 代理生效，类内 this.processDocumentAsync() 是自调用，
     * 会绕过代理导致异步失效（变成同步阻塞上传接口）。用 @Lazy 打破循环依赖后通过代理调用即可。
     */
    @Autowired
    @Lazy
    private RagDocumentService selfProxy;

    // ==================================================================================
    // 1. 上传
    // ==================================================================================

    @Override
    public RagDocument uploadDocument(MultipartFile file, String title, Long userId) throws Exception {
        validateFile(file);
        if (userId == null) {
            throw new RuntimeException("未获取到登录用户信息，请重新登录");
        }

        String originalName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String extension = extractExtension(originalName);

        // 1. 落盘到 D 盘：{userId}_{时间戳}_{原文件名}
        Path docDir = Paths.get(ragProperties.getDocStoragePath());
        Files.createDirectories(docDir);
        String storedName = userId + "_" + System.currentTimeMillis() + "_" + sanitizeFileName(originalName);
        Path target = docDir.resolve(storedName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info(">>> 【RAG】上传文件已保存到 D 盘：{}（{} 字节）", target.toAbsolutePath(), file.getSize());

        // 2. 落库（PENDING）
        RagDocument document = new RagDocument();
        document.setUserId(userId);
        document.setTitle(title == null || title.trim().isEmpty()
                ? stripExtension(originalName) : title.trim());
        document.setFileName(originalName);
        document.setFileType(extension.toUpperCase(Locale.ROOT));
        document.setFileSize(file.getSize());
        document.setFilePath(target.toAbsolutePath().toString());
        document.setChunkCount(0);
        document.setSourceType(SOURCE_USER);
        document.setStatus(STATUS_PENDING);
        document.setIsDelete(0);
        ragDocumentMapper.insert(document);

        // 3. 异步解析 + 向量化（不阻塞上传接口响应）
        //    必须通过代理调用，否则 @Async 失效
        selfProxy.processDocumentAsync(document.getId());

        return document;
    }

    // ==================================================================================
    // 2. 异步解析与向量化
    // ==================================================================================

    @Override
    @Async
    public void processDocumentAsync(Long documentId) {
        RagDocument document = ragDocumentMapper.selectById(documentId);
        if (document == null) {
            log.warn(">>> 【RAG】异步处理跳过：文档不存在，id={}", documentId);
            return;
        }
        markStatus(documentId, STATUS_PROCESSING, null);

        try {
            String text;
            if (SOURCE_SCENIC.equals(document.getSourceType())) {
                text = buildScenicTextById(document);
            } else {
                text = parseFileToText(Paths.get(document.getFilePath()), document.getFileType());
            }

            int chunkCount = indexDocument(document, text);
            markStatus(documentId, STATUS_COMPLETED, null);
            updateChunkCount(documentId, chunkCount);
            log.info(">>> 【RAG】文档向量化完成：id={}，title={}，片段数={}",
                    documentId, document.getTitle(), chunkCount);
        } catch (Exception e) {
            log.error(">>> 【RAG】文档向量化失败：id={}，原因={}", documentId, e.getMessage(), e);
            markStatus(documentId, STATUS_FAILED, truncate(e.getMessage(), 500));
        }
    }

    // ==================================================================================
    // 3. 删除
    // ==================================================================================

    @Override
    public void deleteDocument(Long documentId, Long userId) {
        RagDocument document = ragDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在或已被删除");
        }
        // 权限校验：只能删除自己上传的文档；系统预置/景点同步文档（userId=0）不可删除
        if (document.getUserId() == null || !document.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该文档（系统预置知识库不可删除）");
        }

        // 1. 移除向量
        removeVectors(documentId);
        // 2. 逻辑删除数据库记录（@TableLogic 自动置 is_delete=1）
        ragDocumentMapper.deleteById(documentId);
        // 3. 删除本地文件（D盘）
        deleteLocalFile(document.getFilePath());

        log.info(">>> 【RAG】文档已删除：id={}，title={}", documentId, document.getTitle());
    }

    // ==================================================================================
    // 4. 列表
    // ==================================================================================

    @Override
    public List<RagDocument> listDocuments(Long userId) {
        // 当前用户上传的文档 + 系统预置/景点同步的共享文档（sourceType != USER）
        // is_delete 由 @TableLogic 自动过滤
        LambdaQueryWrapper<RagDocument> wrapper = new LambdaQueryWrapper<>();
        if (userId == null) {
            wrapper.ne(RagDocument::getSourceType, SOURCE_USER);
        } else {
            wrapper.and(w -> w.eq(RagDocument::getUserId, userId)
                    .or().ne(RagDocument::getSourceType, SOURCE_USER));
        }
        wrapper.orderByDesc(RagDocument::getCreateTime);
        return ragDocumentMapper.selectList(wrapper);
    }

    // ==================================================================================
    // 5. 景点一键同步
    // ==================================================================================

    @Override
    public int syncScenicToKnowledgeBase() throws Exception {
        List<ScenicSimple> scenicList = scenicSimpleMapper.selectList(null);
        if (scenicList == null || scenicList.isEmpty()) {
            log.warn(">>> 【RAG】景点同步：scenic 表没有可用数据");
            return 0;
        }

        int successCount = 0;
        for (ScenicSimple scenic : scenicList) {
            try {
                String text = buildScenicText(scenic);
                if (text.trim().isEmpty()) {
                    continue;
                }

                // 已存在同名 SCENIC 文档 → 先清掉旧的向量与记录，保证幂等（重复点同步不会产生重复片段）
                removeExistingScenicDocuments(scenic.getTitle());

                RagDocument document = new RagDocument();
                document.setUserId(SYSTEM_USER_ID);
                document.setTitle(scenic.getTitle());
                document.setFileName(null);
                document.setFileType(SOURCE_SCENIC);
                document.setFileSize((long) text.getBytes(StandardCharsets.UTF_8).length);
                document.setFilePath(null);   // 景点文档不落盘，内容直接来自数据库
                document.setChunkCount(0);
                document.setSourceType(SOURCE_SCENIC);
                document.setStatus(STATUS_PROCESSING);
                document.setIsDelete(0);
                ragDocumentMapper.insert(document);

                int chunkCount = indexDocument(document, text);
                markStatus(document.getId(), STATUS_COMPLETED, null);
                updateChunkCount(document.getId(), chunkCount);
                successCount++;
            } catch (Exception e) {
                log.error(">>> 【RAG】景点同步失败：title={}，原因={}", scenic.getTitle(), e.getMessage(), e);
            }
        }

        log.info(">>> 【RAG】景点同步完成，成功 {}/{} 个", successCount, scenicList.size());
        return successCount;
    }

    // ==================================================================================
    // 6. 重建索引
    // ==================================================================================

    @Override
    public int reindexAll() throws Exception {
        log.info(">>> 【RAG】开始重建全部索引（清空向量库）");
        embeddingStore.removeAll();

        int count = 0;

        // 1. 用户上传/系统预置文档：从本地文件重新解析
        LambdaQueryWrapper<RagDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RagDocument::getSourceType, SOURCE_USER, SOURCE_SYSTEM)
                .eq(RagDocument::getStatus, STATUS_COMPLETED);
        for (RagDocument document : ragDocumentMapper.selectList(wrapper)) {
            try {
                if (document.getFilePath() == null || !Files.exists(Paths.get(document.getFilePath()))) {
                    markStatus(document.getId(), STATUS_FAILED, "重建索引失败：本地文件已不存在");
                    continue;
                }
                String text = parseFileToText(Paths.get(document.getFilePath()), document.getFileType());
                int chunkCount = indexDocument(document, text);
                updateChunkCount(document.getId(), chunkCount);
                count++;
            } catch (Exception e) {
                log.error(">>> 【RAG】重建索引失败：id={}，原因={}", document.getId(), e.getMessage(), e);
                markStatus(document.getId(), STATUS_FAILED, truncate(e.getMessage(), 500));
            }
        }

        // 2. 景点文档：直接从 scenic 表重建
        LambdaQueryWrapper<RagDocument> scenicWrapper = new LambdaQueryWrapper<>();
        scenicWrapper.eq(RagDocument::getSourceType, SOURCE_SCENIC);
        for (RagDocument document : ragDocumentMapper.selectList(scenicWrapper)) {
            try {
                String text = buildScenicTextById(document);
                if (text.trim().isEmpty()) {
                    continue;
                }
                int chunkCount = indexDocument(document, text);
                markStatus(document.getId(), STATUS_COMPLETED, null);
                updateChunkCount(document.getId(), chunkCount);
                count++;
            } catch (Exception e) {
                log.error(">>> 【RAG】景点文档重建索引失败：id={}，原因={}", document.getId(), e.getMessage(), e);
                markStatus(document.getId(), STATUS_FAILED, truncate(e.getMessage(), 500));
            }
        }

        log.info(">>> 【RAG】重建索引完成，共处理 {} 个文档", count);
        return count;
    }

    // ==================================================================================
    // 内部工具方法
    // ==================================================================================

    /**
     * 把文本切分、向量化并写入向量库
     *
     * @return 切分出的片段数量
     */
    private int indexDocument(RagDocument document, String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn(">>> 【RAG】文档内容为空，跳过向量化：id={}", document.getId());
            return 0;
        }

        // 1. 切分
        Document langChainDocument = Document.from(text);
        List<TextSegment> rawSegments = documentSplitter.split(langChainDocument);
        if (rawSegments == null || rawSegments.isEmpty()) {
            log.warn(">>> 【RAG】切分结果为空，跳过向量化：id={}", document.getId());
            return 0;
        }

        // 2. 重建带 metadata 的片段
        //    显式覆盖 metadata，而不是依赖切分器对 Document.metadata 的透传，避免版本差异
        List<TextSegment> segments = new ArrayList<>(rawSegments.size());
        for (int i = 0; i < rawSegments.size(); i++) {
            TextSegment raw = rawSegments.get(i);
            Metadata metadata = new Metadata();
            metadata.put(META_DOCUMENT_ID, String.valueOf(document.getId()));
            metadata.put(META_TITLE, document.getTitle() == null ? "" : document.getTitle());
            metadata.put(META_FILE_TYPE, document.getFileType() == null ? "" : document.getFileType());
            // 统一以字符串存放，避免不同向量库序列化后读回类型不一致
            metadata.put(META_CHUNK_INDEX, String.valueOf(i));
            segments.add(TextSegment.from(raw.text(), metadata));
        }

        // 3. 批量向量化
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // 4. 写入向量库
        embeddingStore.addAll(embeddings, segments);
        return segments.size();
    }

    /**
     * 按文件类型选择解析器并解析为纯文本
     */
    private String parseFileToText(Path filePath, String fileType) throws IOException {
        try (InputStream in = Files.newInputStream(filePath)) {
            String type = fileType == null ? "" : fileType.toUpperCase(Locale.ROOT);
            switch (type) {
                case "PDF" -> {
                    DocumentParser parser = new ApachePdfBoxDocumentParser();
                    return parser.parse(in).text();
                }
                case "DOCX", "XLSX" -> {
                    DocumentParser parser = new ApachePoiDocumentParser();
                    return parser.parse(in).text();
                }
                case "TXT" -> {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                default -> throw new IOException("不支持的文件类型：" + fileType);
            }
        }
    }

    /**
     * 依据景点ID从数据库重建景点知识文本
     */
    private String buildScenicTextById(RagDocument document) {
        LambdaQueryWrapper<ScenicSimple> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSimple::getTitle, document.getTitle());
        List<ScenicSimple> list = scenicSimpleMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            log.warn(">>> 【RAG】景点已不存在，跳过：title={}", document.getTitle());
            return "";
        }
        return buildScenicText(list.get(0));
    }

    /**
     * 把一条景点数据拼装为知识库文本
     *
     * 【与需求文档的差异】文档假设字段为 name/description/open_time/ticket_price，
     * 实际表结构是 title/introduce/times/tag/address，这里按真实字段拼装。
     */
    private String buildScenicText(ScenicSimple scenic) {
        if (scenic == null || scenic.getTitle() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("景点名称：").append(scenic.getTitle()).append("\n");

        if (notBlank(scenic.getIntroduce())) {
            sb.append("景点介绍：").append(scenic.getIntroduce()).append("\n");
        }
        if (notBlank(scenic.getTimes())) {
            sb.append("开放时间：").append(scenic.getTimes()).append("\n");
        }
        String tags = joinJsonArray(scenic.getTag());
        if (notBlank(tags)) {
            sb.append("景点标签：").append(tags).append("\n");
        }
        String address = joinJsonArray(scenic.getAddress());
        if (notBlank(address)) {
            sb.append("坐标位置：").append(address).append("\n");
        }
        return sb.toString();
    }

    /**
     * 把 JSON 数组文本（如 ["著名","名胜古迹"]）拼成可读的中文顿号分隔字符串
     */
    private String joinJsonArray(String jsonArrayText) {
        if (!notBlank(jsonArrayText)) {
            return "";
        }
        try {
            JSONArray array = JSONUtil.parseArray(jsonArrayText);
            return array.stream()
                    .map(String::valueOf)
                    .filter(this::notBlank)
                    .collect(Collectors.joining("、"));
        } catch (Exception e) {
            // 不是合法 JSON 时原样返回，保证不丢信息
            return jsonArrayText;
        }
    }

    /**
     * 移除指定文档在向量库中的所有片段
     */
    private void removeVectors(Long documentId) {
        try {
            Filter filter = metadataKey(META_DOCUMENT_ID).isEqualTo(String.valueOf(documentId));
            embeddingStore.removeAll(filter);
        } catch (UnsupportedOperationException e) {
            // Redis 向量存储可能不支持按 filter 删除，此时只能提示重建索引
            log.warn(">>> 【RAG】当前向量存储不支持按文档删除（id={}），"
                    + "该文档片段仍留在向量库中，请调用 /api/rag/reindex 重建索引。原因：{}",
                    documentId, e.getMessage());
        } catch (Exception e) {
            log.warn(">>> 【RAG】删除向量失败（id={}）：{}", documentId, e.getMessage());
        }
    }

    /**
     * 删除已有的同名景点文档（保证景点同步幂等）
     */
    private void removeExistingScenicDocuments(String title) {
        LambdaQueryWrapper<RagDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RagDocument::getSourceType, SOURCE_SCENIC)
                .eq(RagDocument::getTitle, title);
        for (RagDocument old : ragDocumentMapper.selectList(wrapper)) {
            removeVectors(old.getId());
            ragDocumentMapper.deleteById(old.getId());
        }
    }

    private void deleteLocalFile(String filePath) {
        if (!notBlank(filePath)) {
            return;
        }
        try {
            Path path = Paths.get(filePath);
            // 只允许删除位于配置目录下的文件，防止误删其他路径
            Path docDir = Paths.get(ragProperties.getDocStoragePath()).toAbsolutePath().normalize();
            if (path.toAbsolutePath().normalize().startsWith(docDir)) {
                boolean deleted = Files.deleteIfExists(path);
                log.info(">>> 【RAG】本地文件{}：{}", deleted ? "已删除" : "不存在", filePath);
            } else {
                log.warn(">>> 【RAG】拒绝删除配置目录之外的文件：{}", filePath);
            }
        } catch (Exception e) {
            log.warn(">>> 【RAG】删除本地文件失败：{}，原因={}", filePath, e.getMessage());
        }
    }

    private void markStatus(Long documentId, String status, String errorMsg) {
        RagDocument update = new RagDocument();
        update.setId(documentId);
        update.setStatus(status);
        update.setErrorMsg(errorMsg);
        update.setUpdateTime(LocalDateTime.now());
        ragDocumentMapper.updateById(update);
    }

    private void updateChunkCount(Long documentId, int chunkCount) {
        RagDocument update = new RagDocument();
        update.setId(documentId);
        update.setChunkCount(chunkCount);
        update.setUpdateTime(LocalDateTime.now());
        ragDocumentMapper.updateById(update);
    }

    /**
     * 文件校验：扩展名 + MIME + 大小
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超过限制（最大 10MB）");
        }

        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("不支持的文件类型：." + extension
                    + "，仅支持 " + String.join("/", ALLOWED_EXTENSIONS));
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isEmpty()
                && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("文件 MIME 类型不合法：" + contentType);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String stripExtension(String fileName) {
        if (fileName == null) {
            return "未命名文档";
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    /**
     * 清洗文件名，去掉路径分隔符等危险字符，防止目录穿越
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown";
        }
        String cleaned = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        cleaned = cleaned.replace("..", "_");
        return cleaned.length() > 120 ? cleaned.substring(cleaned.length() - 120) : cleaned;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
