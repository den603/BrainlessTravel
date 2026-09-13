package net.togogo.travel.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.travel.ai.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
}