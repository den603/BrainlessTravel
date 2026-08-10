package net.togogo.springboot_travel.vo;

import lombok.Data;
import java.time.LocalDateTime;
/**
 * 组成员信息视图对象
 * 用于展示组队详情中的成员列表
 * 包含用户基本信息和在组队中的角色
 */
@Data
public class TeamMemberVO {
    private Long userId;
    private String nickName;
    private String avatarUrl;
    private Integer role;      // 1-队长，2-队员
    private LocalDateTime joinTime;
}