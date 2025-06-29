package com.example.PrepaidSolution.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF for non-browser clients like Postman
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests without auth
                );
        return http.build();
    }
}