package net.togogo.travel.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import net.togogo.travel.user.entity.Scenic;
import net.togogo.travel.user.mapper.ScenicMapper;
import net.togogo.travel.user.service.ScenicService;
import net.togogo.travel.user.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 景点 Service 实现类
 *
 * 【缓存架构 - Cache Aside 模式】
 * 读流程：先读缓存 → 缓存有则返回 → 缓存无则读数据库 → 写入缓存 → 返回
 * 写流程：先写数据库 → 写成功后删除缓存 → 下次读自动加载最新数据
 *
 * 【缓存 Key 设计】
 * scenic:list        → 景点列表（List<Scenic>）
 * scenic:detail:{id} → 单个景点详情（Scenic）
 */
@Service
public class ScenicServiceImpl extends ServiceImpl<ScenicMapper, Scenic> implements ScenicService {

    /**
     * 注入 Redis 工具类，用于操作缓存
     */
    @Resource
    private RedisUtil redisUtil;

    // ========== 缓存 Key 常量（集中管理，防止拼写错误，方便维护）==========

    /** 景点列表在 Redis 中的缓存键 */
    private static final String CACHE_KEY_LIST = "scenic:list";

    /** 景点详情缓存键的前缀，完整 key = scenic:detail:{id} */
    private static final String CACHE_KEY_DETAIL_PREFIX = "scenic:detail:";

    /** 缓存过期时间：30 分钟。防止缓存永久存在导致数据不一致 */
    private static final long CACHE_TIMEOUT = 30;

    /**
     * 获取景点列表（带 Redis 缓存）
     *
     * 【执行流程】
     * 1. 构造缓存 key："scenic:list"
     * 2. 查询 Redis，有数据直接返回
     * 3. Redis 没有，查询 MySQL 数据库
     * 4. 将数据库结果写入 Redis，设置 30 分钟过期时间
     * 5. 返回数据
     */
    @Override
    public List<Scenic> getListWithCache() {
        // 步骤1：尝试从 Redis 读取缓存
        Object cacheObj = redisUtil.get(CACHE_KEY_LIST);

        if (cacheObj != null) {
            // 【缓存命中】Redis 里有数据，直接返回，不走数据库，响应极快
            System.out.println(">>> 【Redis 缓存命中】获取景点列表");
            return (List<Scenic>) cacheObj;
        }

        // 【缓存未命中】步骤2：查询 MySQL 数据库
        System.out.println(">>> 【MySQL 数据库查询】获取景点列表");
        List<Scenic> list = this.list();

        // 步骤3：将查询结果写入 Redis，设置 30 分钟过期
        redisUtil.set(CACHE_KEY_LIST, list, CACHE_TIMEOUT, TimeUnit.MINUTES);

        return list;
    }

    /**
     * 根据 ID 获取景点详情（带 Redis 缓存）
     *
     * 【执行流程】
     * 1. 构造该景点专属的缓存 key，如 "scenic:detail:1"
     * 2. 查询 Redis，命中直接返回
     * 3. 未命中则查询 MySQL
     * 4. 查到后写入 Redis（查不到则不缓存，防止缓存穿透）
     */
    @Override
    public Scenic getDetailWithCache(Integer id) {
        // 每个景点的缓存 key 是独立的，避免不同景点数据互相覆盖
        String cacheKey = CACHE_KEY_DETAIL_PREFIX + id;

        // 步骤1：查 Redis
        Object cacheObj = redisUtil.get(cacheKey);

        if (cacheObj != null) {
            System.out.println(">>> 【Redis 缓存命中】获取景点详情，ID=" + id);
            return (Scenic) cacheObj;
        }

        // 【缓存未命中】步骤2：查 MySQL
        System.out.println(">>> 【MySQL 数据库查询】获取景点详情，ID=" + id);
        Scenic scenic = this.getById(id);

        // 步骤3：只有查到真实数据才放入缓存
        // 如果 scenic == null 也缓存，可能被恶意请求攻击（缓存穿透），这里简化处理不缓存空值
        if (scenic != null) {
            redisUtil.set(cacheKey, scenic, CACHE_TIMEOUT, TimeUnit.MINUTES);
        }

        return scenic;
    }

    /**
     * 清除景点相关缓存
     *
     * 【触发时机】
     * - 新增景点：旧列表已不全，必须清缓存
     * - 修改景点：列表和详情都可能脏了，必须清缓存
     * - 删除景点：同上
     *
     * 【当前实现】
     * 只清除了列表缓存 scenic:list。
     * 详情缓存（scenic:detail:xx）要么等 30 分钟自动过期，
     * 要么在修改/删除具体景点时单独清除该条缓存。
     */
    @Override
    public void clearScenicCache() {
        redisUtil.delete(CACHE_KEY_LIST);
        System.out.println(">>> 【Redis】景点列表缓存已清除，key=" + CACHE_KEY_LIST);
    }
}