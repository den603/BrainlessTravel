package net.togogo.springboot_travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.togogo.springboot_travel.entity.User;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {

    /**
     * 小程序登录：根据code获取token
     */
    String loginByWeChat(String code);

    /**
     * 根据openid获取用户信息
     */
    User getUserByOpenid(String openid);

    /**
     * 根据 openid 获取用户信息（带 Redis 缓存）
     *
     * 【为什么需要这个方法？】
     * LoginInterceptor 每次请求都要查用户信息确认身份，这是最高频的查询。
     * 走 Redis 缓存后，拦截器性能提升 10 倍以上，MySQL 压力大幅降低。
     */
    User getUserByOpenidWithCache(String openid);

    /**
     * 用户登出，清除 Redis 中的登录状态
     *
     * 【作用】
     * 1. 删除 Redis 中的 token，使该 token 立即失效（即使 JWT 还没过期）
     * 2. 将 token 加入黑名单，防止在 JWT 过期前被恶意使用
     */
    void logout(String token);

    /**
     * 更新用户昵称和头像
     */
    boolean updateUserInfo(String openid, String nickName, String avatarUrl);
}