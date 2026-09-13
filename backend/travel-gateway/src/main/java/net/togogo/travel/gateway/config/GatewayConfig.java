package net.togogo.travel.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关通用配置
 *
 * 【说明】
 * 路由规则、Nacos 注册发现等静态配置全部在 application.yml 中声明（见文档 7.4 节），
 * 本类只承载需要在 Java 侧注册的全局组件 —— 目前是统一访问日志过滤器。
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * 网关统一访问日志：记录请求方法、路径、响应状态与耗时。
     *
     * 【实现说明】Spring Cloud Gateway 4.1.x 中 GlobalFilter 与 GatewayFilter
     * 是两个**互不相干**的接口（GlobalFilter 不再继承 GatewayFilter），
     * 因此这里不能用 OrderedGatewayFilter 包装，改为定义一个同时实现
     * GlobalFilter + Ordered 的内部类来指定优先级。
     */
    @Bean
    public GlobalFilter accessLogGlobalFilter() {
        return new AccessLogGlobalFilter();
    }

    /**
     * 访问日志全局过滤器（最高优先级，最先进入、最后离开，能统计到含鉴权在内的全部耗时）
     */
    private static final class AccessLogGlobalFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            long startMillis = System.currentTimeMillis();
            String method = exchange.getRequest().getMethod().name();
            String path = exchange.getRequest().getURI().getPath();

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        long cost = System.currentTimeMillis() - startMillis;
                        Integer status = exchange.getResponse().getStatusCode() != null
                                ? exchange.getResponse().getStatusCode().value()
                                : null;
                        log.info(">>> 【网关访问日志】{} {} -> 状态={} 耗时={}ms", method, path, status, cost);
                    }));
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
