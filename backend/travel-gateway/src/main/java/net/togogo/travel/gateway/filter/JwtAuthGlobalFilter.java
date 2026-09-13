package net.togogo.travel.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.togogo.travel.common.Result.Result;
import net.togogo.travel.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 网关 JWT 全局鉴权过滤器
 *
 * 【职责】
 * 1. 白名单路径直接放行（/api/login/**，即登录、注册、微信授权）
 * 2. 从请求头 Authorization 提取 Bearer token
 * 3. 用 JwtUtil 校验 token 签名与过期时间，解析出 userId
 * 4. 将 userId 写入请求头 X-User-Id 后转发给下游服务
 * 5. token 无效或缺失时直接返回 401 JSON（Result.fail("未登录或token已过期")）
 *
 * 【安全要点】
 * 转发前强制移除客户端自带的 X-User-Id 请求头，防止伪造用户身份越权。
 *
 * 【白名单说明 —— 与需求文档的差异，已与项目方确认】
 * 需求文档原定「白名单仅 /api/login/**」，但小程序首页的轮播图、景点列表、
 * 景点详情、游玩项目、银发模式列表、寻伴大厅都是**游客态**请求的，
 * 原单体架构下这些接口也全部不做鉴权（原 WebMvcConfig 只拦截 /plan、/qa、/chat）。
 * 若严格执行只放行 /api/login/**，未登录用户的首页会直接空白。
 * 因此白名单改为**可配置**（jwt.white-list），默认只放行登录接口 + 改造前
 * 就已公开的只读查询接口。相比之下安全性是**增强**的：
 * 原单体下 /scenic/add、/scenic/update、/scenic/delete、/upload/scenic
 * 均无任何鉴权，改造后这些写接口全部要求携带 token。
 */
@Slf4j
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    /** 网关写入下游服务的用户ID请求头 */
    public static final String USER_ID_HEADER = "X-User-Id";

    private static final String BEARER_PREFIX = "Bearer ";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 免鉴权白名单（Ant 风格路径，英文逗号分隔）。
     * 默认值仅放行登录接口，保证即使配置缺失也不会把鉴权放开。
     */
    @Value("${jwt.white-list:/api/login/**}")
    private String whiteListConfig;

    /** 解析后的白名单缓存（配置在 Nacos 中可刷新，这里按首次访问解析即可） */
    private volatile List<String> whiteListPatterns;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. CORS 预检请求直接放行（预检不带 Authorization）
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // 2. 白名单放行
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 3. 提取 Authorization 请求头
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            log.warn(">>> 【网关鉴权失败】缺少 Authorization 请求头，path={}", path);
            return unauthorized(exchange, "未登录或token已过期");
        }

        // 4. 校验 token 签名与有效期
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!jwtUtil.validateToken(token)) {
            log.warn(">>> 【网关鉴权失败】token 无效或已过期，path={}", path);
            return unauthorized(exchange, "token已过期或无效");
        }

        // 5. 解析 userId
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            // 改造前签发的旧 token 载荷中只有 openid，没有 userId，无法透传用户身份
            log.warn(">>> 【网关鉴权失败】token 载荷中不含 userId（旧格式 token），path={}", path);
            return unauthorized(exchange, "token已失效，请重新登录");
        }

        // 6. 写入 X-User-Id 转发给下游服务；先 remove 再 set，杜绝客户端伪造
        String userIdValue = String.valueOf(userId);
        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.set(USER_ID_HEADER, userIdValue);
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 判断是否属于白名单路径（Ant 风格匹配，支持 /api/login/** 这类通配）
     */
    private boolean isWhiteList(String path) {
        for (String pattern : getWhiteListPatterns()) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 懒解析白名单配置，避免每个请求都做字符串切分
     */
    private List<String> getWhiteListPatterns() {
        List<String> patterns = this.whiteListPatterns;
        if (patterns == null) {
            synchronized (this) {
                patterns = this.whiteListPatterns;
                if (patterns == null) {
                    patterns = new ArrayList<>();
                    for (String item : whiteListConfig.split(",")) {
                        String trimmed = item.trim();
                        if (!trimmed.isEmpty()) {
                            patterns.add(trimmed);
                        }
                    }
                    this.whiteListPatterns = patterns;
                }
            }
        }
        return patterns;
    }

    /**
     * 返回 401 JSON 响应（统一 Result 格式，前端可无差别解析）
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(Result.fail(msg));
        } catch (Exception e) {
            // 极端情况下退化为手写 JSON，保证鉴权失败一定有可解析响应体
            body = ("{\"code\":0,\"msg\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 数值越小优先级越高，鉴权必须在路由转发之前执行
        return -100;
    }
}
