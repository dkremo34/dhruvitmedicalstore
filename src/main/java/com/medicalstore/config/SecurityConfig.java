package com.medicalstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/css/**", "/js/**", "/image/**", "/login", "/register", "/").permitAll()
                    .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                )
                .headers(headers -> headers
                    .frameOptions().deny()
                    .contentTypeOptions().and()
                    .httpStrictTransportSecurity(hsts -> hsts
                        .maxAgeInSeconds(31536000)
                        .includeSubDomains(true)
                    )
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .csrf(csrf -> csrf
                    .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers("/inventory/update", "/search", "/medicine/**", "/inventory/dashboard", "/inventory/stock-details", "/api/backup/**")
                );
            
            logger.info("Security configuration initialized successfully");
            return http.build();
        } catch (Exception e) {
            logger.error("Failed to configure security", e);
            throw new RuntimeException("Security configuration failed", e);
        }
    }
}