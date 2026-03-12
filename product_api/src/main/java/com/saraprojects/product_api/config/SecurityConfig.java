package com.saraprojects.product_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disables CSRF (to make testing in Postman easier)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // temporarily grants access to all endpoints
                );

        return http.build();
    }
}
