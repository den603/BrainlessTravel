package net.togogo.travel.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.togogo.travel.user.entity.Scenic;

import java.util.List;

public interface ScenicService  extends IService<Scenic> {

    /**
     * 获取全部景点列表（带 Redis 缓存）
     *
     * 【缓存策略 - Cache Aside】
     * 1. 先读 Redis，命中直接返回（微秒级响应）
     * 2. Redis 未命中，读 MySQL，然后写入 Redis（设置 30 分钟过期）
     * 3. 下次请求直接从 Redis 返回
     *
     * @return 景点实体列表
     */
    List<Scenic> getListWithCache();

    /**
     * 根据 ID 获取景点详情（带 Redis 缓存）
     *
     * 【缓存策略】
     * 每个景点单独缓存，key 格式：scenic:detail:{id}
     * 例如：scenic:detail:1、scenic:detail:2
     *
     * @param id 景点 ID
     * @return 景点实体；不存在返回 null
     */
    Scenic getDetailWithCache(Integer id);

    /**
     * 清除景点相关缓存
     *
     * 【什么时候调用？】
     * 新增、修改、删除景点后必须调用！
     * 原因：数据库数据已变，缓存里的旧数据必须清除，否则前端会读到脏数据。
     *
     * 【为什么用"删缓存"而不是"更新缓存"？】
     * 并发场景下，"更新缓存"可能出现竞态条件导致脏数据。
     * "删缓存"更简单安全，下次读请求会自动从数据库加载最新数据。
     */
    void clearScenicCache();
}
