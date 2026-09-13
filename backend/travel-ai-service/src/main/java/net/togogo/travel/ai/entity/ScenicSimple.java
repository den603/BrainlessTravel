package net.togogo.travel.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 景点精简实体（ai-service 专用，仅用于「景点一键同步知识库」）
 *
 * ============================================================================
 * 【为什么要有这个类】
 * ai-service 需要景点数据来做知识库同步。为减少服务间依赖，**不通过 Feign 调
 * user-service**，而是直连同一个 MySQL 库查询（见文档 9.5.7）。
 * 但完整 Scenic 实体/Service 迁过来会重复且臃肿，因此这里只映射同步所需的字段。
 *
 * 【与需求文档的差异 —— 重要，已按真实表结构修正】
 * 文档 9.5.7 假设 scenic 表字段为 name/description/address/open_time/ticket_price，
 * 但实际库表（见 sql/travel_init.sql）字段为：
 *   id, title, img, tag(json), is_dot, dot, introduce, times, is_play, address(json), is_delete
 * 其中 **不存在** name、description、open_time、ticket_price 四列。
 * 因此本类按真实列名映射：
 *   title     → 景点名称
 *   introduce → 景点简介
 *   times     → 开放时间
 *   tag       → 标签（JSON 数组文本）
 *   address   → 经纬度（JSON 数组文本）
 *
 * 【为什么 tag / address 用 String 而不用 List】
 * 这两列是 MySQL 的 json 类型。原 Scenic 实体用 FastjsonTypeHandler 映射为 List，
 * 但需要在 @TableName 上开启 autoResultMap 才能对 SELECT 生效，容易踩坑。
 * 这里直接按 JSON 文本读取，使用时用 Hutool 解析，最稳妥且无隐藏依赖。
 * ============================================================================
 */
@Data
@TableName("scenic")
public class ScenicSimple {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 景点名称 */
    private String title;

    /** 景点简介 */
    private String introduce;

    /** 开放时间（如 08:00-18:00） */
    private String times;

    /** 标签，JSON 数组文本（如 ["著名","名胜古迹"]） */
    private String tag;

    /** 经纬度，JSON 数组文本（如 ["116.410886","39.881949"]） */
    private String address;

    @TableLogic
    private Integer isDelete;
}
