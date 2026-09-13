package net.togogo.travel.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户业务微服务启动类（端口 8081）
 *
 * 【关键注意事项】
 * 1. @ComponentScan 必须显式包含 net.togogo.travel.common，
 *    否则 travel-common 中的 JwtUtil Bean 不会被注册
 *    （@SpringBootApplication 默认只扫描启动类所在包及子包）；
 * 2. @EnableFeignClients 用于调用 travel-ai-service 的星火大模型能力
 *    （问答模块 QaServiceImpl 需要生成题目与评价）；
 * 3. 用户ID统一通过 UserContext.getUserId() 获取，由 UserIdInterceptor 从
 *    网关下发的 X-User-Id 请求头填充。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "net.togogo.travel.user.feign")
@MapperScan("net.togogo.travel.user.mapper")
@ComponentScan(basePackages = {"net.togogo.travel.user", "net.togogo.travel.common"})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
