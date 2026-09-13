package net.togogo.travel.ai.config;

import jakarta.annotation.Resource;
import net.togogo.travel.ai.util.UserIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类（ai-service）
 *
 * 【微服务改造说明】
 * 原单体 WebConfig 注册 LoginInterceptor 并拦截 /plan/**、/qa/**、/chat/**；
 * 现在鉴权由网关统一负责，本服务只注册 UserIdInterceptor，
 * 把网关下发的 X-User-Id 写入 UserContext 供业务层使用。
 *
 * 【为什么排除 /internal/**
 * /internal/** 是 user-service 通过 Feign 调用的服务内部接口（如星火大模型转发），
 * 不走网关、也没有用户上下文，无需参与拦截器逻辑。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private UserIdInterceptor userIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userIdInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/internal/**");
    }
}
