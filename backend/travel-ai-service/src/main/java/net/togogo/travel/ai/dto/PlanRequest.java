package net.togogo.travel.ai.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 旅行计划生成请求参数 DTO
 * 用于接收前端表单模式提交的数据
 */
@Data
public class PlanRequest {
    private String departure;           // 出发地
    private String destination;         // 目的地（必填）
    private LocalDate travelDate;       // 出行日期
    private Integer days;               // 旅行天数
    private Integer peopleCount;        // 同行人数
    private String peopleType;          // 同行类型
    private Integer budgetMin;          // 最低预算
    private Integer budgetMax;          // 最高预算
    private List<String> preferences;   // 兴趣偏好标签列表
}