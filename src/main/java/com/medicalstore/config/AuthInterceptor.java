package com.medicalstore.config;

import com.medicalstore.model.UserDetails;
import com.medicalstore.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // Allow access to public pages and static resources
        if (uri.equals("/") || uri.equals("/login") || uri.equals("/register") ||
            uri.startsWith("/css/") || uri.startsWith("/image/") || uri.startsWith("/js/") ||
            uri.startsWith("/api-docs") || uri.startsWith("/swagger-ui")) {
            return true;
        }
        
        // Check if user is logged in
        if (request.getSession().getAttribute(Constants.USER_SESSION_KEY) == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Check admin-only paths
        for (String adminPath : Constants.ADMIN_PATHS) {
            if (uri.startsWith(adminPath)) {
                Object role = request.getSession().getAttribute(Constants.USER_ROLE_KEY);
                if (!UserDetails.Role.ADMIN.equals(role)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                    return false;
                }
                break;
            }
        }
        
        return true;
    }
}