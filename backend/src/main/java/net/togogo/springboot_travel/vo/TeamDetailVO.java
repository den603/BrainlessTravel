package net.togogo.springboot_travel.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 组队详情视图对象
 * 包含完整的组队信息、创建人信息以及成员列表
 * 成员列表使用 TeamMemberVO 嵌套表示
 */
@Data
public class TeamDetailVO {
    private Long id;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalPeople;
    private Integer currentPeople;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    // 创建人信息
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
    // 成员列表
    private List<TeamMemberVO> members;
}