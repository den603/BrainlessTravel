package net.togogo.springboot_travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.togogo.springboot_travel.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper（MyBatis-Plus自动实现CRUD）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}