package net.togogo.travel.user.config;

import jakarta.annotation.Resource;
import net.togogo.travel.user.util.UserIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类（user-service）
 *
 * 【微服务改造说明】
 * 原单体 WebConfig 注册的是 LoginInterceptor，并只拦截 /plan/**、/qa/**、/chat/**；
 * 路径与会话校验现在统一由网关负责，本服务改为注册 {@link UserIdInterceptor}，
 * 作用范围扩大到 /**，只做「把 X-User-Id 请求头写入 UserContext」这一件事，
 * 不改变任何接口原有的鉴权结果（公开接口依旧公开，因为拦截器不拒绝任何请求）。
 *
 * 原 LoginInterceptor 类仍保留在 util 包中（含单点登录实现），但**不再注册**，
 * 需要恢复单点登录能力时，在此处追加一行 registry.addInterceptor(loginInterceptor) 即可。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private UserIdInterceptor userIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userIdInterceptor)
                .addPathPatterns("/**");
    }
}
