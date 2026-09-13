package net.togogo.travel.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 功能：
 * - 生成 token：将 openid 存入载荷，设置过期时间
 * - 解析 token：从 token 中提取 openid
 * - 验证 token：校验签名和有效期
 *
 * 密钥和过期时间从 application.yml 中读取
 *
 * 【迁移说明】从原单体项目 net.togogo.springboot_travel.util.JwtUtil 原样迁移，
 * 包名改为 net.togogo.travel.common.util，生成/验证 token 逻辑完全不变。
 * 各服务通过 @ComponentScan(basePackages = {"...自家包", "net.togogo.travel.common"})
 * 扫描到本 Bean（见各服务启动类）。
 */
@Component
public class JwtUtil {

    // 从配置文件读取JWT密钥
    @Value("${jwt.secret}")
    private String secret;

    // token有效期
    @Value("${jwt.expire}")
    private long expire;

    /**
     * 生成token（基于openid）
     */
    public String generateToken(String openid) {
        // 过期时间
        Date expireDate = new Date(System.currentTimeMillis() + expire);

        // 构建JWT
        return Jwts.builder()
                // 设置自定义载荷（存储openid）
                .setClaims(new HashMap<String, Object>() {{
                    put("openid", openid);
                }})
                // 设置签发时间
                .setIssuedAt(new Date())
                // 设置过期时间
                .setExpiration(expireDate)
                // 设置签名密钥
                .signWith(getSecretKey())
                // 紧凑序列化
                .compact();
    }

    /**
     * 【微服务改造新增重载】生成token（同时写入 openid 与 userId）
     *
     * 【为什么必须加这个重载】
     * 改造后 JWT 校验统一收敛到网关层（travel-gateway），网关需要把 userId 写入
     * 请求头 X-User-Id 转发给下游服务。但网关无数据库连接，无法用 openid 反查 user.id，
     * 因此必须在登录签发 token 时就带上 userId，网关才能零额外查询地解析出用户ID。
     * 原 generateToken(String openid) 方法签名与行为完全保留，不做任何修改。
     */
    public String generateToken(String openid, Long userId) {
        Date expireDate = new Date(System.currentTimeMillis() + expire);

        Map<String, Object> claims = new HashMap<>();
        claims.put("openid", openid);
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 【微服务改造新增】从 token 载荷中解析 userId（网关层使用）
     *
     * @return 用户ID；token 为改造前签发的旧格式（不含 userId 声明）时返回 null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object userId = claims.get("userId");
        if (userId == null) {
            return null;
        }
        if (userId instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(userId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析token，获取载荷中的openid
     */
    public String getOpenidFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("openid", String.class);
    }

    /**
     * 验证token是否有效（未过期、签名正确）
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 解析失败则token无效
            return false;
        }
    }

    /**
     * 将字符串密钥转换为JWT要求的SecretKey
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
