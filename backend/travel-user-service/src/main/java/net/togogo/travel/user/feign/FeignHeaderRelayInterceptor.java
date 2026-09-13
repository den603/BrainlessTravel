package net.togogo.travel.user.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求头透传拦截器
 *
 * 【为什么需要】
 * user-service 通过 Feign 调用 ai-service 时是「服务直连」（走 Nacos 服务发现），
 * 不经过网关，因此网关下发的 X-User-Id 与前端携带的 Authorization 都不会自动带上。
 * 本拦截器把当前请求的这两个头原样透传给 ai-service，
 * 保证 ai-service 侧的 UserContext 也能取到同一个登录用户。
 *
 * 【注意】本类不加 @Configuration 注解，避免被当成全局 Feign 配置影响其他客户端
 * （Spring Cloud 规定：@FeignClient 的 configuration 类不应被 @ComponentScan 扫成 @Configuration）。
 */
public class FeignHeaderRelayInterceptor implements RequestInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 非 Web 线程（如定时任务、异步线程）发起的调用，无请求头可透传
            return;
        }
        String userId = attributes.getRequest().getHeader(USER_ID_HEADER);
        if (userId != null) {
            template.header(USER_ID_HEADER, userId);
        }
        String authorization = attributes.getRequest().getHeader(AUTHORIZATION_HEADER);
        if (authorization != null) {
            template.header(AUTHORIZATION_HEADER, authorization);
        }
    }
}
