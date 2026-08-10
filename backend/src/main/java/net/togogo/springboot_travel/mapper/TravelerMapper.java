package net.togogo.springboot_travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.springboot_travel.entity.Traveler;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TravelerMapper extends BaseMapper<Traveler> {
}