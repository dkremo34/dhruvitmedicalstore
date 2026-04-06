package com.medicalstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = false)
public class AuthenticationFilter implements Filter {

    private static final String[] PUBLIC_URLS = {"/", "/login", "/register", "/css/", "/js/", "/image/"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect("/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicUrl(String path) {
        for (String url : PUBLIC_URLS) {
            if (path.equals(url) || path.startsWith(url)) {
                return true;
            }
        }
        return false;
    }
}