package net.togogo.travel.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 网关 CORS 跨域配置
 *
 * 【关键注意事项】
 * 1. 网关基于 WebFlux，只能使用 CorsWebFilter，
 *    严禁使用 Spring MVC 的 WebMvcConfigurer（类路径上根本没有该自动配置）。
 * 2. CORS 统一在网关层处理，下游服务不再各自配置跨域。
 * 3. 全站跨域由本类唯一负责。application.yml 中的
 *    spring.cloud.gateway.globalcors 已同步注释掉，避免两套机制同时生效
 *    导致响应头出现重复的 Access-Control-Allow-Origin（浏览器会直接判定跨域失败）。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（使用 pattern 形式，兼容 allow-credentials=true）
        config.addAllowedOriginPattern("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许携带 Cookie / Authorization
        config.setAllowCredentials(true);
        // 暴露给前端的响应头
        config.addExposedHeader("*");
        // 预检请求缓存 1 小时
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
