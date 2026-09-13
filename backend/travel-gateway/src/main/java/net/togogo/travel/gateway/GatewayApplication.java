package net.togogo.travel.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * API 网关启动类（端口 8080）
 *
 * 【关键注意事项】
 * 1. 本模块基于 Spring Cloud Gateway（WebFlux），严禁引入 spring-boot-starter-web；
 * 2. @ComponentScan 必须显式包含 net.togogo.travel.common，
 *    否则 travel-common 中的 JwtUtil Bean 不会被注册（默认只扫描启动类所在包及子包）；
 * 3. 显式声明 @ComponentScan 会覆盖 @SpringBootApplication 的默认扫描规则，
 *    因此两个包都必须列全。
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"net.togogo.travel.gateway", "net.togogo.travel.common"})
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
