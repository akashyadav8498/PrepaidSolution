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
import org.springframework.security.config.http.SessionCreationPolicy;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin","/logout","/owner","/tenant","/sendOTP","/verifyOTP", "/index.html","/admin_index.html",
                                "/dashboard","/api/meter/rooms/by-pg/{pgId}", "/api/meter/add_tenant","/admin_dashboard.html","/",
                                "/service-worker.js", "/manifest.json", "/addUser","/api/meter/dashboard", "/api/meter/get_onload_data",
                                "/api/meter/{meterId}", "/api/meter/pg/by-owner/{ownerId}","/api/meter/add_meter/{roomId}",
                                "/api/owners/stats","/api/owners/{ownerId}/pgs", "/api/owners/pg/{pgId}/rooms","/balance/add", "/balance/deduct",
                                "/test.html", "/ws/**","/api/notifications/**", "/api/tenants/tenant-data", "/api/tenants/logout"
                        ).permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")               // The URL that triggers logout
                        .logoutSuccessUrl("/")    // Where to go after logout
                        .invalidateHttpSession(true)        // Destroy session on server
                        .clearAuthentication(true)          // Clear SecurityContext
                        .deleteCookies("JSESSIONID")       // Delete the 30-day cookie
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
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
