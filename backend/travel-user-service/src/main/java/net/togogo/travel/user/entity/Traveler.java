package net.togogo.travel.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_traveler")
public class Traveler {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    private String phone;
    private String idCard;
    private Long userId;
    @TableLogic
    private Integer isDelete;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}