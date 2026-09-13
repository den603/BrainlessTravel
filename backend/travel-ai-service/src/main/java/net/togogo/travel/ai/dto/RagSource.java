package net.togogo.travel.ai.dto;

import lombok.Data;

/**
 * RAG 引用来源 DTO
 * 表示 AI 回答所引用的一个知识库片段，用于前端「📚 参考来源」展示。
 */
@Data
public class RagSource {

    /** 所属知识库文档ID */
    private Long documentId;

    /** 文档标题 */
    private String title;

    /** 文档类型：PDF/DOCX/TXT/XLSX/SCENIC */
    private String fileType;

    /** 该片段在文档中的序号（从0开始） */
    private Integer chunkIndex;

    /** 片段摘要（前200字符） */
    private String content;

    /** 相似度得分（0~1，越大越相关） */
    private Double score;
}
