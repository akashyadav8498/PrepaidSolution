package com.example.PrepaidSolution.config;

import com.example.PrepaidSolution.component.AuthSuccessHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin","/owner","/tenant","/sendOTP","/verifyOTP", "/index.html","/admin_index.html",
                                "/dashboard","/api/meter/rooms/by-pg/{pgId}", "/api/meter/add_tenant","/admin_dashboard.html","/",
                                "/service-worker.js", "/manifest.json", "/addUser","/api/meter/dashboard", "/api/meter/get_onload_data",
                                "/api/meter/{meterId}", "/api/meter/pg/by-owner/{ownerId}","/api/meter/add_meter/{roomId}",
                                "/api/owners/{ownerId}/stats","/api/owners/{ownerId}/pgs"
                        ).permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated()
                ).formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthSuccessHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
