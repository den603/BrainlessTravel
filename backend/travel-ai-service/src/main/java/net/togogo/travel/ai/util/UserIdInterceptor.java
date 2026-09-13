package net.togogo.travel.ai.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.common.util.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户ID拦截器（ai-service）
 *
 * 【设计说明】与 user-service 的同名类完全一致：
 * 只负责把网关下发的 X-User-Id 请求头写入 {@link UserContext}，
 * 不做任何 JWT 解析、数据库查询或 Redis 查询。
 *
 * 之所以两个服务各写一份，是因为它们分属不同的 Spring 容器（不同进程），
 * 且两个模块都不依赖彼此；放在 travel-common 反而会让公共模块染上 Servlet 依赖。
 */
@Slf4j
@Component
public class UserIdInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader(USER_ID_HEADER);
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                UserContext.setUserId(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                log.warn(">>> 【用户上下文】X-User-Id 请求头格式非法：{}", userIdStr);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
