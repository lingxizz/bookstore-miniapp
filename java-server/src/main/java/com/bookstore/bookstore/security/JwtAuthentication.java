package com.bookstore.bookstore.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class JwtAuthentication extends UsernamePasswordAuthenticationToken {

    private final Integer userId;
    private final String openid;

    public JwtAuthentication(Integer userId, String openid) {
        super(userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.userId = userId;
        this.openid = openid;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getOpenid() {
        return openid;
    }
}
