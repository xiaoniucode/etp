package com.xiaoniucode.etp.server.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);

    private final String secret;
    private final long expiration;

    public TokenUtil(@Value("${jwt.secret:}") String secret, 
                    @Value("${jwt.expiration:3600}") long expiration) {
        this.expiration = expiration;
        
        if (!StringUtils.hasText(secret)) {
            // 生成32字节的随机密钥
            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);
            // 转换为Base64字符串
            this.secret = Base64.getEncoder().encodeToString(randomBytes);
            logger.info("JWT密钥未配置，已自动生成32字节随机密钥");
        } else {
            this.secret = secret;
            logger.info("使用配置的JWT密钥");
        }
        logger.info("JWT令牌过期时间设置为: {}秒", expiration);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("created", now);

        String token = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        logger.debug("为用户 {} 生成JWT令牌，过期时间: {}", username, expiryDate);
        return token;
    }

    public Claims getClaimsFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            logger.debug("解析JWT令牌成功，用户: {}", claims.getSubject());
            return claims;
        } catch (Exception e) {
            logger.warn("解析JWT令牌失败: {}", e.getMessage());
            throw e;
        }
    }

    public String getUsernameFromToken(String token) {
        String username = getClaimsFromToken(token).getSubject();
        logger.debug("从JWT令牌中获取用户名: {}", username);
        return username;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            boolean isValid = !claims.getExpiration().before(new Date());
            if (isValid) {
                logger.debug("JWT令牌验证通过，用户: {}", claims.getSubject());
            } else {
                logger.debug("JWT令牌已过期，用户: {}", claims.getSubject());
            }
            return isValid;
        } catch (Exception e) {
            logger.warn("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
}
