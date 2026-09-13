package net.togogo.travel.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * RAG 知识库文档实体
 * 对应数据库表：rag_document
 *
 * 【字段说明】
 * - userId     ：上传用户ID；系统预置数据（景点同步）固定为 0
 * - filePath   ：原始文件在**本地**的绝对路径（D:\dev-resources\rag-docs\...），
 *                本项目知识点文档不走 MinIO，直接落本地磁盘以减少依赖
 * - sourceType ：USER（用户上传）/ SYSTEM（系统预置）/ SCENIC（景点同步）
 * - status     ：PENDING → PROCESSING → COMPLETED / FAILED
 */
@Data
@TableName("rag_document")
public class RagDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 上传用户ID（系统预置为0） */
    private Long userId;

    /** 文档标题 */
    private String title;

    /** 原始文件名 */
    private String fileName;

    /** 文档类型：PDF/DOCX/TXT/XLSX/SCENIC */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 本地存储路径（D盘） */
    private String filePath;

    /** 切分片段数量 */
    private Integer chunkCount;

    /** 来源类型：USER/SYSTEM/SCENIC */
    private String sourceType;

    /** 处理状态：PENDING/PROCESSING/COMPLETED/FAILED */
    private String status;

    /** 失败原因 */
    private String errorMsg;

    @TableLogic
    private Integer isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
