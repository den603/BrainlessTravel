package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_travel_group")
public class TravelGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDate startTime;
    private LocalDate endTime;
    private BigDecimal price;
    private String groupName;
    private String meetingPoint;
    private String guideName;
    private String guidePhone;
    private String guideExperience;
    private String groupInfo;
    @TableLogic
    private Integer isDelete;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}