package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 组队信息实体类
 * 对应数据库表：team
 *
 * 字段说明：
 * - creatorId: 创建人用户ID，关联 user 表的 id
 * - status: 组队状态（1-招募中，2-已满员，3-已结束，4-已取消）
 * - isDelete: 逻辑删除字段（0-未删除，1-已删除），MyBatis-Plus 自动处理
 *
 * 关联关系：
 * - 通过 creatorId 关联 User 实体
 * - 通过 TeamMember 实体维护成员关系
 */
@Data
@TableName("team")
public class Team {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long creatorId;

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalPeople;

    private Integer currentPeople;

    private String description;

    private Integer status; // 1-招募中，2-已满员，3-已结束，4-已取消

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;
}