package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 轮播图实体类
 * 对应数据库表：banner
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("banner")
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID（主键）
     */
    @TableId(type = IdType.ASSIGN_UUID) // 适配字符串主键，自动生成UUID
    private String id;

    /**
     * 轮播图图片地址

     */

    private String image;

    /**
     * 轮播图标题（前端showTitle需要）

     */

    private String title;


//  逻辑删除标记

    @TableLogic
    private Integer is_delete;
}