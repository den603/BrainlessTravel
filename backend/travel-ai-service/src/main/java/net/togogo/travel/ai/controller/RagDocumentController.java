package net.togogo.travel.ai.controller;

import jakarta.annotation.Resource;
import net.togogo.travel.ai.dto.RagSource;
import net.togogo.travel.ai.entity.RagDocument;
import net.togogo.travel.ai.service.RagDocumentService;
import net.togogo.travel.ai.service.RagService;
import net.togogo.travel.common.Result.Result;
import net.togogo.travel.common.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 知识库管理控制器
 *
 * 基础路径：/api/rag（网关路由到 travel-ai-service，服务内 context-path=/api）
 * 所有接口均需携带 token，用户ID由网关解析后通过 X-User-Id 下发。
 */
@Slf4j
@RestController
@RequestMapping("/rag")
public class RagDocumentController {

    @Resource
    private RagDocumentService ragDocumentService;

    @Resource
    private RagService ragService;

    /**
     * 上传知识库文档
     * POST /api/rag/document/upload  （multipart/form-data：file + title）
     */
    @PostMapping("/document/upload")
    public Result<RagDocument> uploadDocument(@RequestParam("file") MultipartFile file,
                                              @RequestParam("title") String title) {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.fail("未登录或登录已过期");
            }
            return Result.success(ragDocumentService.uploadDocument(file, title, userId));
        } catch (Exception e) {
            log.error(">>> 【RAG】文档上传失败：{}", e.getMessage(), e);
            return Result.fail("文档上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除知识库文档（只能删除自己上传的）
     * DELETE /api/rag/document/{id}
     */
    @DeleteMapping("/document/{id}")
    public Result<String> deleteDocument(@PathVariable Long id) {
        try {
            ragDocumentService.deleteDocument(id, UserContext.getUserId());
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error(">>> 【RAG】文档删除失败：{}", e.getMessage(), e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 查询知识库文档列表
     * GET /api/rag/document/list
     */
    @GetMapping("/document/list")
    public Result<List<RagDocument>> listDocuments() {
        try {
            return Result.success(ragDocumentService.listDocuments(UserContext.getUserId()));
        } catch (Exception e) {
            log.error(">>> 【RAG】文档列表查询失败：{}", e.getMessage(), e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    /**
     * 景点一键同步知识库
     * POST /api/rag/sync/scenic
     */
    @PostMapping("/sync/scenic")
    public Result<Integer> syncScenic() {
        try {
            return Result.success(ragDocumentService.syncScenicToKnowledgeBase());
        } catch (Exception e) {
            log.error(">>> 【RAG】景点同步失败：{}", e.getMessage(), e);
            return Result.fail("同步失败：" + e.getMessage());
        }
    }

    /**
     * 重建全部索引
     * POST /api/rag/reindex
     */
    @PostMapping("/reindex")
    public Result<Integer> reindex() {
        try {
            return Result.success(ragDocumentService.reindexAll());
        } catch (Exception e) {
            log.error(">>> 【RAG】重建索引失败：{}", e.getMessage(), e);
            return Result.fail("重新索引失败：" + e.getMessage());
        }
    }

    /**
     * 纯检索测试（不调用大模型）
     * GET /api/rag/retrieve?query=故宫门票
     */
    @GetMapping("/retrieve")
    public Result<List<RagSource>> retrieve(@RequestParam String query) {
        try {
            return Result.success(ragService.retrieve(query));
        } catch (Exception e) {
            log.error(">>> 【RAG】检索失败：{}", e.getMessage(), e);
            return Result.fail("检索失败：" + e.getMessage());
        }
    }
}
