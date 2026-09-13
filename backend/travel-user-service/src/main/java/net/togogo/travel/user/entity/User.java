package net.togogo.travel.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体类
 */
@Data
@TableName("user") // 数据库表名（需提前创建）
public class User {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微信openid（唯一标识）
     */
    private String openid;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0=未删除，1=已删除
     */
    private Integer isDelete;
}