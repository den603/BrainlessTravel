package net.togogo.springboot_travel.util;

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