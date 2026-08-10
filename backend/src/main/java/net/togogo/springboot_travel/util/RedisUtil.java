package net.togogo.springboot_travel.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * 【作用】
 * 对 RedisTemplate 做一层薄封装，提供项目中常用的增删改查方法。
 * 业务层（Service）直接调用此类方法，代码更简洁，不用每次都写冗长的 redisTemplate.opsForValue().xxx()。
 *
 * 【使用方式】
 * 在 Service 或 Controller 中注入：
 * @Resource
 * private RedisUtil redisUtil;
 */
@Component // 标记为 Spring 组件，由 IOC 容器管理，可被 @Resource 注入
public class RedisUtil {

    /**
     * 注入由 RedisConfig 配置好的 RedisTemplate
     * key 为 String 类型，value 为 Object 类型（可存任意 Java 对象）
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // ========================== String 类型操作（最常用）==========================

    /**
     * 存入键值对，永久有效（直到手动删除或 Redis 重启）
     *
     * @param key   缓存键，如 "scenic:list"
     * @param value 缓存值，任意 Java 对象（会被 JSON 序列化）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存入键值对，并设置过期时间
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时长
     * @param unit    时间单位，如 TimeUnit.MINUTES（分钟）、TimeUnit.SECONDS（秒）
     *
     * 【应用场景】
     * 景点列表缓存 30 分钟：redisUtil.set("scenic:list", list, 30, TimeUnit.MINUTES);
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 根据 key 获取值
     *
     * @param key 缓存键
     * @return 缓存值（Object 类型，需自行强转或用下方泛型方法）
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 根据 key 获取值，并自动转换为指定类型
     *
     * @param key   缓存键
     * @param clazz 目标类型，如 Scenic.class、List.class
     * @param <T>   泛型
     * @return 转换后的对象；key 不存在时返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : clazz.cast(value);
    }

    /**
     * 删除指定 key
     *
     * @param key 缓存键
     * @return true=删除成功，false=key 不存在或删除失败
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除多个 key
     *
     * @param keys key 的集合
     * @return 实际删除的 key 数量
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 缓存键
     * @return true=存在，false=不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 给已存在的 key 设置过期时间
     *
     * @param key     缓存键
     * @param timeout 过期时长
     * @param unit    时间单位
     * @return true=设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    // ========================== List 类型操作 ==========================

    /**
     * 从列表左侧（头部）插入一个元素
     *
     * @param key   列表键
     * @param value 元素值
     * @return 插入后列表的总长度
     */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 从列表左侧（头部）弹出一个元素
     *
     * @param key 列表键
     * @return 弹出的元素；列表为空时返回 null
     */
    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 获取列表的长度
     *
     * @param key 列表键
     * @return 列表元素个数
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ========================== Hash 类型操作 ==========================

    /**
     * 向 Hash 中存入一个字段
     *
     * @param key     Hash 的键
     * @param hashKey Hash 中的字段名
     * @param value   字段值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 从 Hash 中获取一个字段的值
     *
     * @param key     Hash 的键
     * @param hashKey Hash 中的字段名
     * @return 字段值；不存在返回 null
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 删除 Hash 中的指定字段
     *
     * @param key      Hash 的键
     * @param hashKeys 要删除的字段名（可变参数，可传多个）
     * @return 实际删除的字段数量
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }
}