package com.mouad.train.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF protection for REST APIs (or enable it based on your needs)
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection
                .authorizeRequests()
                .anyRequest().permitAll(); // Allow all requests
        return http.build();
    }
}
