package net.togogo.springboot_travel.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 组队列表项视图对象
 * 用于大厅列表和我的组队列表的数据展示
 * 相比实体类，额外包含了创建人的昵称和头像信息，减少前端二次请求
 */
@Data
public class TeamListVO {
    private Long id;

    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalPeople;
    private Integer currentPeople;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    // 发布人信息
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
}