package com.medicalstore.util;

import com.medicalstore.model.UserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

public class SessionUtil {
    
    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(Constants.USER_SESSION_KEY) != null;
    }
    
    public static void addUserToModel(Model model, HttpSession session) {
        if (session != null) {
            model.addAttribute("user", session.getAttribute(Constants.USER_SESSION_KEY));
        }
    }
    
    public static String redirectIfNotLoggedIn(HttpSession session) {
        return isLoggedIn(session) ? null : "redirect:/login";
    }
    
    public static void invalidateSession(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    public static boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute(Constants.USER_ROLE_KEY);
        return UserDetails.Role.ADMIN.equals(role);
    }
}