package net.togogo.travel.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 组队成员关系实体类
 * 对应数据库表：team_member
 *
 * 字段说明：
 * - teamId: 组队ID，关联 team 表的 id
 * - userId: 用户ID，关联 user 表的 id
 * - role: 成员角色（1-队长，2-队员）
 * - isDelete: 逻辑删除字段，退出组队时逻辑删除该记录
 *
 * 唯一约束：team_id + user_id 联合唯一，防止重复加入
 */
@Data
@TableName("team_member")
public class TeamMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teamId;

    private Long userId;

    private Integer role; // 1-队长，2-队员

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;

    @TableLogic
    private Integer isDelete;
}