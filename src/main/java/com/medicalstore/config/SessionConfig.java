package com.medicalstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.SessionCookieConfig;

@Configuration
public class SessionConfig {

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean secureCookie;

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            SessionCookieConfig config = servletContext.getSessionCookieConfig();
            config.setHttpOnly(true);
            config.setSecure(secureCookie);
            config.setMaxAge(1800);
        };
    }
}
