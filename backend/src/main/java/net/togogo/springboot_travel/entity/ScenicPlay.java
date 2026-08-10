package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scenic_play")
public class ScenicPlay {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer scenicId; // 关联景区ID
    private String title;    // 项目名称
    private String url;      // 项目图片
    private String tag;      // 项目标签
    private String description; // 项目描述/地址
    private Integer sort;    // 排序
    private Integer status;  // 状态
}