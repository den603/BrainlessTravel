package net.togogo.travel.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_guide_booking")
public class GuideBooking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long guideId;
    private Long userId;
    @TableLogic
    private Integer isDelete;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}