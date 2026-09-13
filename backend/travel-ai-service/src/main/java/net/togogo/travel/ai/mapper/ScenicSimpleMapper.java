package net.togogo.travel.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.travel.ai.entity.ScenicSimple;
import org.apache.ibatis.annotations.Mapper;

/**
 * 景点精简 Mapper（ai-service 专用）
 *
 * 【说明】ai-service 与 user-service 连接的是同一个 MySQL 库，
 * 因此这里可以直接查 scenic 表，无需通过 Feign 调用 user-service，
 * 减少服务间耦合（见文档 9.5.7）。本 Mapper 只做只读查询。
 */
@Mapper
public interface ScenicSimpleMapper extends BaseMapper<ScenicSimple> {
}
