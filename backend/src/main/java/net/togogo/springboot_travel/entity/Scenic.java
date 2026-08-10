package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 景点实体类
 * 对应数据库表：scenic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("scenic") // 补充MyBatis-Plus表名映射
public class Scenic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 景点ID（主键）
     */
    @TableId(type = IdType.AUTO) // 自增主键
    private Integer id;

    /**
     * 景点标题（如：天坛公园）
     */

    private String title;

    /**
     * 景点主图路径（如：/static/tt.jpg）
     */

    private String img;

    /**
     * 景点标签数组（如：['著名', '名胜古迹']）
     */

    @TableField(value = "tag", typeHandler = FastjsonTypeHandler.class)
    private List<String> tag;

    /**
     * 推荐标记（如：推荐）
     */

    private String is_dot;

    /**
     * 是否显示红点（true-显示，false-不显示）
     */
    private Boolean dot;

    /**
     * 景点详细介绍
     */

    private String introduce;

    /**
     * 开放时间（如：08:00-18:00）
     */

    private String times;

    /**
     * 是否有游玩项目
     */

    private Boolean is_play;

    /**
     * 经纬度数组（如：['116.410886', '39.881949']）
     */

    @TableField(value = "address", typeHandler = FastjsonTypeHandler.class)
    private List<String> address;

    @TableLogic
    @JsonIgnore  // 不返回给前端
    private Integer is_delete;

}