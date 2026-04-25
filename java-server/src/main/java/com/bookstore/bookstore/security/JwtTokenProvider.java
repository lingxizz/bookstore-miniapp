package com.bookstore.bookstore.security;

import com.bookstore.bookstore.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final SecretKey key;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        byte[] bytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                bytes = md.digest(bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(Integer userId, String openid) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getExpiration());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("openid", openid)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
