package net.togogo.travel.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.travel.user.entity.Traveler;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TravelerMapper extends BaseMapper<Traveler> {
}