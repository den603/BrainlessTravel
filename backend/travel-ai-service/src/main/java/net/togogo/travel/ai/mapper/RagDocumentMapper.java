package net.togogo.travel.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.travel.ai.entity.RagDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * RAG 知识库文档 Mapper
 * 继承 MyBatis-Plus BaseMapper，全部走参数化查询，无 SQL 注入风险。
 */
@Mapper
public interface RagDocumentMapper extends BaseMapper<RagDocument> {
}
