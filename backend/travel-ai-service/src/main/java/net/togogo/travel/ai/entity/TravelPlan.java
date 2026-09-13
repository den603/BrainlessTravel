package net.togogo.travel.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 旅行计划实体类
 * 对应数据库表：t_travel_plan
 * 关联用户表 user，通过 user_id 字段建立外键关系
 */
@Data
@TableName("t_travel_plan")
public class TravelPlan {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键ID，自增

    private Long userId;            // 用户ID，关联 user 表的 id 字段

    private String departure;       // 出发地（选填）

    private String destination;     // 目的地（必填）

    private LocalDate travelDate;   // 出行日期

    private Integer days;           // 旅行天数

    private Integer peopleCount;    // 同行人数

    private String peopleType;      // 同行类型：情侣/家庭/朋友/独自

    private Integer budgetMin;      // 最低预算（单位：元）

    private Integer budgetMax;      // 最高预算（单位：元）

    private String preferences;     // 兴趣偏好，存储为 JSON 数组字符串，如 ["美食","自然风光"]

    private String planContent;     // AI 生成的完整计划内容，存储为 JSON 字符串

    private BigDecimal totalBudget; // AI 估算的总花费

    private LocalDateTime createdAt;// 创建时间

    private LocalDateTime updatedAt;// 更新时间
}