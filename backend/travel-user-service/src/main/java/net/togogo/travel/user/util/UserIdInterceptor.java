package net.togogo.travel.user.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.common.util.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户ID拦截器（user-service）
 *
 * 【设计说明】
 * 微服务改造后，JWT 的校验统一收敛到网关层（travel-gateway）。
 * 网关校验通过后把 userId 写入请求头 X-User-Id 转发给下游服务，
 * 本拦截器只负责把该请求头读出并写入 {@link UserContext}，
 * **不再做任何 JWT 解析、数据库查询或 Redis 查询**，零额外开销。
 *
 * 【为什么不用原来的 LoginInterceptor】
 * 原 LoginInterceptor 每次请求都要「查 Redis 黑名单 + 查 Redis 单点登录状态 + 查用户」，
 * 是单体架构下的高频开销；微服务化后这部分职责前移到网关。
 */
@Slf4j
@Component
public class UserIdInterceptor implements HandlerInterceptor {

    /** 网关注入的用户ID请求头 */
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader(USER_ID_HEADER);
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                UserContext.setUserId(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                // 请求头被篡改或格式异常时保持未登录状态，交由业务层判断
                log.warn(">>> 【用户上下文】X-User-Id 请求头格式非法：{}", userIdStr);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理，防止线程池复用导致用户身份串号
        UserContext.clear();
    }
}
