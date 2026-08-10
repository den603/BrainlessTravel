package net.togogo.springboot_travel.dto;

import lombok.Data;
import java.time.LocalDate;
/**
 * 组队大厅查询筛选参数 DTO
 * 支持多条件组合查询：
 * - keyword: 目的地或描述模糊搜索
 * - destination: 精确目的地筛选（前端下拉选择）
 * - date: 查询指定日期在行程范围内的组队
 * - peopleRange: 人数范围筛选（1:2人, 2:3-5人, 3:6-10人, 4:10人以上）
 */
@Data
public class TeamQueryDTO {
    private String keyword;         // 搜索关键词
    private String destination;     // 目的地筛选
    private LocalDate date;         // 日期筛选
    private Integer peopleRange;    // 人数范围：1-2人，2-3-5人，3-6-10人，4-10人以上
}