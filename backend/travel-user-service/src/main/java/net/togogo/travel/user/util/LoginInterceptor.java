package net.togogo.travel.user.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.togogo.travel.common.util.JwtUtil;
import net.togogo.travel.common.util.UserContext;
import net.togogo.travel.user.entity.User;
import net.togogo.travel.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器（集成 Redis 校验 + 单点登录）
 *
 * ============================================================================
 * 【微服务改造说明 —— 重要，请勿直接注册为拦截器】
 * 1. 本类在微服务架构下**不再注册**到拦截器链（见 WebMvcConfig），
 *    取而代之的是 {@link UserIdInterceptor}：它直接从网关下发的
 *    请求头 X-User-Id 读取用户ID，零数据库/Redis 查询。
 * 2. 因此下面的 preHandle 逻辑当前不会被执行。它被完整保留，是为了：
 *    - 保留原有单点登录（Redis user:token:{openid}）与 token 黑名单的实现，
 *      便于后续需要时在 WebMvcConfig 中一行注册即可恢复；
 *    - 改造后这两项能力默认关闭，属于已知限制（同一账号可多设备同时在线）。
 * 3. getCurrentUserId() 已改为委托 UserContext.getUserId()，
 *    保证历史调用点无论走哪条链路都能取到正确的用户ID。
 * ============================================================================
 *
 * 【校验流程】
 * 1. 从请求头取 Authorization
 * 2. 查 Redis 黑名单（被登出的 token 直接拒绝）
 * 3. 验证 JWT 签名和有效期，解析出 openid
 * 4. 查 Redis 用户登录状态（核心：单点登录校验）
 *    - Redis 中没有 → 已过期或已登出
 *    - Redis 中有但不等于当前 token → 在别处重新登录了（被挤下线）
 * 5. 从 Redis 缓存获取用户信息
 * 6. 将 userId 存入 ThreadLocal
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    /** Token 黑名单前缀 */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 【优化】用户 Token 映射前缀
     * 完整 key = user:token:{openid}，value = JWT token
     */
    private static final String USER_TOKEN_KEY_PREFIX = "user:token:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"未登录或token无效\"}");
            return false;
        }
        token = token.replace("Bearer ", "");

        // ========== 步骤1：查 Redis 黑名单 ==========
        String blackKey = TOKEN_BLACKLIST_PREFIX + token;
        if (Boolean.TRUE.equals(redisUtil.hasKey(blackKey))) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"token已被登出，请重新登录\"}");
            return false;
        }

        // ========== 步骤2：验证 JWT 并解析 openid ==========
        if (!jwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"token已过期或无效\"}");
            return false;
        }
        String openid = jwtUtil.getOpenidFromToken(token);

        // ========== 步骤3：查 Redis 用户登录状态（单点登录核心）==========
        String userTokenKey = USER_TOKEN_KEY_PREFIX + openid;
        Object storedTokenObj = redisUtil.get(userTokenKey);

        if (storedTokenObj == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"登录已过期或已在其他设备登出\"}");
            return false;
        }

        // 【单点登录校验】Redis 中存的 token 必须与当前请求的一致
        // 如果不一致，说明用户在别处重新登录了，当前 token 被新 token 覆盖，已失效
        String storedToken = (String) storedTokenObj;
        if (!token.equals(storedToken)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"账号已在其他设备登录\"}");
            return false;
        }

        // ========== 步骤4：从 Redis 缓存获取用户信息 ==========
        User user = userService.getUserByOpenidWithCache(openid);
        if (user == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"用户不存在\"}");
            return false;
        }

        currentUserId.set(user.getId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        currentUserId.remove();  // 防止内存泄漏
    }

    /**
     * 获取当前登录用户 ID（供业务层使用）
     *
     * 【微服务改造说明】统一委托给 UserContext.getUserId()。
     * 微服务架构下 userId 由网关解析 JWT 后通过请求头 X-User-Id 下发，
     * 再由各服务的 UserIdInterceptor 写入 UserContext。
     * 本方法保留仅为兼容历史调用点，新代码请直接使用 UserContext.getUserId()。
     */
    public static Long getCurrentUserId() {
        return UserContext.getUserId();
    }
}