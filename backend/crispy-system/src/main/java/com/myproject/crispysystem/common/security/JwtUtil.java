package com.myproject.crispysystem.common.security;

import com.myproject.crispysystem.constants.Constants;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private static final String JWT_BLACKLIST_PREFIX = "jwt_blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final Key key;

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    public JwtUtil(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
        this.key = Keys.hmacShaKeyFor(Constants.getJwtSecret().getBytes());
    }

    /**
     * Generate JWT token for the given subject (e.g, user ID).
     *
     * @param subject The subject of the token (e.g, user ID),
     * @return The generated JWT token.
     */
    public String generateToken(String subject){
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty.");
        }
        try {
            return Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + Constants.getJwtExpiry()))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to generate token.", e);
        }
    }

    /**
     * Validates JWT token.
     *
     * @param token JWT token to validate
     * @return True if the token valid, and false if invalid
     */
    public boolean validateToken(String token){
        if (isTokenBlacklisted(token)){
            logger.warn("Token is blacklisted: {}", token);
            return false;
        }
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        }catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        }catch (IllegalArgumentException e) {
            logger.error("JWY claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Get the subject from JWT token.
     *
     * @param token The JWT token.
     * @return The subject of the token
     */
    public String getSubjectFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public void blacklistToken(String token) {
        String tokenKey = JWT_BLACKLIST_PREFIX + token;
        long expiryDuration = getExpiryDuration(token);
        if (expiryDuration > 0) {
            redisTemplate.opsForValue().set(tokenKey, "blacklisted", expiryDuration, TimeUnit.MILLISECONDS);
            logger.info("Token blacklisted: {}" ,token);
        }
    }

    private boolean isTokenBlacklisted(String token) {
        String tokenKey = JWT_BLACKLIST_PREFIX + token;
        Boolean isBlacklisted = redisTemplate.hasKey(tokenKey);
        return Boolean.TRUE.equals(isBlacklisted);
    }

    public long getExpiryDuration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (JwtException e) {
            logger.error("Error parsing token for expiry: {}", e.getMessage());
            return 0;
        }
    }
}
