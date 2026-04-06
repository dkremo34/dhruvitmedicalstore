package com.medicalstore.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,}$");
    
    public static void validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }
    
    public static void validateRegistration(String username, String password, String fullName, String email) {
        validateLogin(username, password);
        if (fullName == null || fullName.trim().length() < 2) {
            throw new IllegalArgumentException("Full name must be at least 2 characters");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address");
        }
    }
    
    public static void validateStrongPassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least 6 characters with uppercase, lowercase, and number");
        }
    }
    
    public static boolean isValidQuantity(Integer quantity) {
        return quantity != null && quantity >= 0;
    }
    
    public static boolean isValidPrice(Double price) {
        return price != null && price >= 0;
    }
}