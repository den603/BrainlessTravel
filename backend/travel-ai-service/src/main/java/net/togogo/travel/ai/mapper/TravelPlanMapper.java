package net.togogo.travel.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.travel.ai.entity.TravelPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 旅行计划 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得 CRUD 方法
 */
@Mapper
public interface TravelPlanMapper extends BaseMapper<TravelPlan> {
}
