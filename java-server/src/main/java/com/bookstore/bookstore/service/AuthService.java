package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.UserRepository;
import com.bookstore.bookstore.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public User findOrCreateUser(String openid, String nickname, String avatar) {
        Optional<User> existing = userRepository.findByOpenid(openid);
        if (existing.isPresent()) {
            return existing.get();
        }
        User user = new User();
        user.setOpenid(openid);
        String defaultNickname = "读者" + (1000 + (int)(Math.random() * 9000));
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : defaultNickname);
        user.setAvatar(avatar != null ? avatar : "");
        return userRepository.save(user);
    }

    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getId(), user.getOpenid());
    }
}
