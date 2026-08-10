package net.togogo.springboot_travel.service.Impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import net.togogo.springboot_travel.entity.User;
import net.togogo.springboot_travel.mapper.UserMapper;
import net.togogo.springboot_travel.service.UserService;
import net.togogo.springboot_travel.util.HttpUtil;
import net.togogo.springboot_travel.util.JwtUtil;
import net.togogo.springboot_travel.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户 Service 实现类（集成 Redis）
 *
 * 【Redis 在登录模块的核心作用】
 * 1. Token 状态存储：登录后将 openid->token 映射写入 Redis，实现后端可控的登录状态
 * 2. 单点登录：同一个用户多次登录，新 token 会覆盖旧 token，旧 token 自动失效
 * 3. 用户信息缓存：拦截器高频查询走 Redis，减少 MySQL 压力
 * 4. 登出失效：删除 Redis 中的映射，让 JWT 提前失效
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${wechat.mini.appid}")
    private String appid;

    @Value("${wechat.mini.appsecret}")
    private String appsecret;

    @Value("${wechat.mini.login-url}")
    private String loginUrl;

    /**
     * JWT 过期时间（毫秒），从 application.yml 读取
     * 用于设置 Redis 中 token 的 TTL，保持与 JWT 一致
     */
    @Value("${jwt.expire}")
    private long jwtExpire;

    @Resource
    private HttpUtil httpUtil;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 注入 Redis 工具类，用于操作缓存
     */
    @Resource
    private RedisUtil redisUtil;

    // ========== 缓存 Key 常量（集中管理，防止拼写错误，方便维护）==========

    /**
     * 【优化】用户 Token 映射前缀
     * 完整 key = user:token:{openid}，value = JWT token
     *
     * 【为什么不用完整 JWT 做 key？】
     * 之前：login:token:{完整JWT} → key 长达 200+ 字符，占内存、难阅读
     * 现在：user:token:{openid}   → key 仅 40 字符左右，简洁高效
     *
     * 【如何实现单点登录？】
     * 同一个 openid 只能对应一个 token，新登录会覆盖旧 token。
     * 拦截器校验时，对比 Redis 中的 token 与请求头的是否一致，
     * 不一致说明在别处登录了，当前 token 失效。
     */
    private static final String USER_TOKEN_KEY_PREFIX = "user:token:";

    /** Token 黑名单前缀，完整 key = token:blacklist:{jwt_token} */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /** 用户信息缓存前缀，完整 key = user:openid:{openid} */
    private static final String USER_CACHE_PREFIX = "user:openid:";

    /** 用户信息缓存过期时间：30 分钟 */
    private static final long USER_CACHE_TIMEOUT = 30;

    /**
     * 小程序登录核心逻辑（集成 Redis）
     *
     * 【执行流程】
     * 1. 调用微信接口，用 code 换 openid
     * 2. 根据 openid 查用户，不存在则新增
     * 3. 生成 JWT token
     * 4. 【Redis】存储 openid -> token 映射（支持单点登录）
     * 5. 【Redis】缓存用户信息
     * 6. 返回 token
     */
    @Override
    public String loginByWeChat(String code) {
        try {
            // 1. 拼接微信接口 URL
            String weChatUrl = String.format(
                    "%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    loginUrl, appid, appsecret, code
            );

            // 2. 调用微信接口获取 openid
            JSONObject result = httpUtil.doGet(weChatUrl);
            if (result == null || result.getStr("openid") == null) {
                throw new RuntimeException("获取openid失败：" + result);
            }
            String openid = result.getStr("openid");

            // 3. 根据 openid 查询用户，不存在则新增
            User user = this.getUserByOpenid(openid);
            if (user == null) {
                user = new User();
                user.setOpenid(openid);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                user.setIsDelete(0);
                this.save(user);
            }

            // 4. 生成 JWT token
            String token = jwtUtil.generateToken(openid);

            // ========== 【优化】Redis 存储 openid -> token 映射 ==========
            // key 格式：user:token:{openid}，value：JWT token
            // 同一个用户再次登录，新 token 会直接覆盖旧 token（天然单点登录）
            String userTokenKey = USER_TOKEN_KEY_PREFIX + openid;
            redisUtil.set(userTokenKey, token, jwtExpire / 1000, TimeUnit.SECONDS);
            System.out.println(">>> 【Redis】用户登录状态已存储，key=" + userTokenKey);

            // ========== Redis 缓存用户信息 ==========
            String userKey = USER_CACHE_PREFIX + openid;
            redisUtil.set(userKey, user, USER_CACHE_TIMEOUT, TimeUnit.MINUTES);

            return token;

        } catch (IOException e) {
            throw new RuntimeException("调用微信接口失败", e);
        }
    }

    /**
     * 根据 openid 查询用户（直接查数据库）
     */
    @Override
    public User getUserByOpenid(String openid) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid)
                .eq(User::getIsDelete, 0);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据 openid 查询用户（带 Redis 缓存）
     *
     * 【执行流程】
     * 1. 先查 Redis，命中直接返回
     * 2. 未命中查 MySQL，然后写入 Redis（30 分钟过期）
     */
    @Override
    public User getUserByOpenidWithCache(String openid) {
        String userKey = USER_CACHE_PREFIX + openid;

        // 步骤1：尝试从 Redis 读取
        Object cacheObj = redisUtil.get(userKey);
        if (cacheObj != null) {
            System.out.println(">>> 【Redis 缓存命中】获取用户信息，openid=" + openid);
            return (User) cacheObj;
        }

        // 【缓存未命中】步骤2：查 MySQL
        System.out.println(">>> 【MySQL 数据库查询】获取用户信息，openid=" + openid);
        User user = this.getUserByOpenid(openid);

        // 步骤3：写入 Redis（只有查到了才缓存）
        if (user != null) {
            redisUtil.set(userKey, user, USER_CACHE_TIMEOUT, TimeUnit.MINUTES);
        }

        return user;
    }

    /**
     * 用户登出
     *
     * 【执行流程】
     * 1. 解析 token 获取 openid
     * 2. 删除 Redis 中的 openid->token 映射（使该用户的所有 token 失效）
     * 3. 将 token 加入黑名单（双重保险）
     */
    @Override
    public void logout(String token) {
        // 1. 解析 token 获取 openid，才能定位到对应的 Redis key
        String openid = jwtUtil.getOpenidFromToken(token);

        // 2. 删除 Redis 中的用户 token 映射
        String userTokenKey = USER_TOKEN_KEY_PREFIX + openid;
        redisUtil.delete(userTokenKey);
        System.out.println(">>> 【Redis】用户登录状态已清除，key=" + userTokenKey);

        // 3. 加入黑名单（防止 JWT 在过期前被继续使用）
        long remainTime = jwtExpire;
        String blackKey = TOKEN_BLACKLIST_PREFIX + token;
        redisUtil.set(blackKey, "logout", remainTime / 1000, TimeUnit.SECONDS);
    }

    /**
     * 更新用户昵称和头像（修改后清除用户缓存，保证数据一致性）
     */
    @Override
    public boolean updateUserInfo(String openid, String nickName, String avatarUrl) {
        User user = this.getUserByOpenid(openid);
        if (user == null) {
            return false;
        }
        user.setNickName(nickName);
        user.setAvatarUrl(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        boolean success = this.updateById(user);

        // 数据库更新成功后，删除 Redis 中的用户缓存
        if (success) {
            String userKey = USER_CACHE_PREFIX + openid;
            redisUtil.delete(userKey);
            System.out.println(">>> 【Redis】用户缓存已清除，key=" + userKey);
        }

        return success;
    }
}