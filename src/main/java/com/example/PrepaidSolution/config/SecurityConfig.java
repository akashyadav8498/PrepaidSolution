package com.example.PrepaidSolution.config;

import com.example.PrepaidSolution.components.security.CustomAccessDeniedHandler;
import com.example.PrepaidSolution.components.security.CustomAuthenticationEntryPoint;
import com.example.PrepaidSolution.components.security.CustomAuthenticationSuccessHandler;
import com.example.PrepaidSolution.components.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
//     @Autowired
//     private Environment env;

    private final AppConfig passwordEncoder;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler; // Inject your custom access denied handler
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(AppConfig passwordEncoder, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler,
                          CustomUserDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.userDetailsService=userDetailsService;
    }


    // Part 2: Who are our users and what roles do they have? (In-memory for simplicity)
//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
//        // We're creating two users: "admin" and "owner"
//        UserDetails user = User.builder()
//                .username("owner")
//                .password(passwordEncoder.encode("password")) // Encode the password!
//                .roles("OWNER") // This user has the "OWNER" role
//                .build();
//
//        UserDetails admin = User.builder()
//                .username("admin")
//                .password(passwordEncoder.encode("adminpass")) // Encode the password!
//                .roles("ADMIN", "OWNER") // This user has both "ADMIN" and "USER" roles
//                .build();
//
//        //This manages users in memory. For a real app, you'd connect to a database.
//        return new InMemoryUserDetailsManager(user, admin);
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        if (Boolean.parseBoolean(env.getRequiredProperty("security.disable.csrf")))
//            http.csrf().disable();
         http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity (careful in production!) // Disable CSRF for non-browser clients like Postman
                 .authorizeHttpRequests(auth -> auth
                         .requestMatchers("/","/**", "/index", "/login", "/js/**", "/css/**", "/images/**").permitAll()
                         .requestMatchers("/admin/**").hasRole("ADMIN")
                         .requestMatchers("/owner/**").hasRole("OWNER")
                         .anyRequest().authenticated()
                 )
                 .formLogin( form -> form
                        .loginPage("/index.html")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/loginError")
                        .permitAll() // Allow everyone to access the login page and related URLs (like /login for POST)

                ).exceptionHandling(exception -> exception
                         .authenticationEntryPoint(customAuthenticationEntryPoint) // Handles 401
                         .accessDeniedHandler(customAccessDeniedHandler)       // Handles 403
                 )
                 .logout(logout -> logout
                         .logoutUrl("/logout") // URL to trigger logout (default is /logout)
                         .logoutSuccessUrl("/login") // Redirect after successful logout
                         .permitAll() // Allow everyone to access logout
                 );
        // .httpBasic(withDefaults()); // Remove or comment out if you only want form login

        return http.build();
    }


    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder.passwordEncoder())
                .and()
                .build();
    }
}