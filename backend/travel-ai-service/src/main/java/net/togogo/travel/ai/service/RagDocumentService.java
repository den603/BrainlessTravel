package net.togogo.travel.ai.service;

import net.togogo.travel.ai.entity.RagDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 知识库文档管理服务接口
 */
public interface RagDocumentService {

    /**
     * 上传文档：保存文件 → 落库(PENDING) → 异步解析切分向量化
     *
     * @param file   上传的文件（仅允许 pdf/docx/txt/xlsx，单文件不超过 10MB）
     * @param title  文档标题
     * @param userId 上传用户ID（来自网关下发的 X-User-Id）
     * @return 已落库的文档记录（此时状态为 PENDING，向量化在后台进行）
     * @throws Exception 文件校验或保存失败时抛出
     */
    RagDocument uploadDocument(MultipartFile file, String title, Long userId) throws Exception;

    /**
     * 异步解析并向量化指定文档（由 uploadDocument 内部触发，不阻塞上传接口）
     *
     * 【注意】必须通过 Spring 代理调用（selfProxy）才会真正异步，
     * 类内直接 this.xxx() 调用会绕过 @Async 代理而变成同步执行。
     *
     * @param documentId 文档ID
     */
    void processDocumentAsync(Long documentId);

    /**
     * 删除文档：校验权限 → 逻辑删除 → 移除向量 → 删除本地文件
     *
     * @param documentId 文档ID
     * @param userId     当前用户ID（只能删除自己上传的文档；系统/景点文档不可删）
     */
    void deleteDocument(Long documentId, Long userId);

    /**
     * 查询知识库文档列表：当前用户上传的 + 系统预置/景点同步的共享文档
     *
     * @param userId 当前用户ID
     * @return 文档列表（按创建时间倒序）
     */
    List<RagDocument> listDocuments(Long userId);

    /**
     * 景点一键同步知识库：把 scenic 表的景点数据索引为 SCENIC 类型知识文档
     *
     * @return 成功同步的景点数量
     * @throws Exception 查询或向量化失败时抛出
     */
    int syncScenicToKnowledgeBase() throws Exception;

    /**
     * 重建全部索引：清空向量库 → 按数据库记录重新解析向量化
     *
     * 【使用场景】memory 内存向量模式重启后向量会丢失，需调用本方法重建。
     *
     * @return 重建成功的文档数量
     * @throws Exception 重建失败时抛出
     */
    int reindexAll() throws Exception;
}
