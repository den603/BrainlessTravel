package net.togogo.travel.ai.dto;

import lombok.Data;

/**
 * RAG 文档上传请求 DTO
 *
 * 【说明】当前上传接口 /api/rag/document/upload 使用
 * multipart/form-data（file + title 两个表单参数），并不直接绑定本类。
 * 本类保留作为「按 JSON 方式提交上传元数据」的扩展载体，
 * 也便于将来接入分片上传等场景。
 */
@Data
public class RagDocumentUploadRequest {

    /** 文档标题（为空时取原始文件名） */
    private String title;

    /** 原始文件名 */
    private String fileName;

    /** 文件类型：PDF/DOCX/TXT/XLSX */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 来源类型：USER/SYSTEM */
    private String sourceType = "USER";
}
