package com.bookstore.bookstore.config;

import com.bookstore.bookstore.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/{id}", "/api/books/{id}/chapters", "/api/books/{bookId}/chapters/{chapterId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/filter").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/banners", "/api/categories", "/api/books/today-pick", "/api/books/hot-rank", "/api/books/guess-like").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/new-releases", "/api/books/completed").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shelf", "/api/books/*/shelf").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/home").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/store").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/search").permitAll()
                .requestMatchers("/api/books/{id}/progress").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/chapters/{id}", "/api/chapters/{id}/comments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                .requestMatchers("/api/admin/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
