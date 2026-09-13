package net.togogo.travel.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.travel.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper（MyBatis-Plus自动实现CRUD）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}