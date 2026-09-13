package net.togogo.travel.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_private_guide")
public class PrivateGuide {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String avatar;
    private String region;
    private String languages;
    private BigDecimal rating;
    private String intro;
    private String experience;
    private BigDecimal price;
    private String phone;
    private String schedule;
    @TableLogic
    private Integer isDelete;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}