package net.togogo.springboot_travel.dto;

import lombok.Data;
import java.time.LocalDate;
/**
 * 发布组队请求参数 DTO
 * 用于接收前端创建组队时提交的数据
 *
 * 字段校验建议：可在 Controller 层或 Service 层进行非空/日期逻辑校验
 */
@Data
public class TeamCreateDTO {
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalPeople;
    private String description;
}