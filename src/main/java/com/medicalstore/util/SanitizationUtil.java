package com.medicalstore.util;

import org.springframework.web.util.HtmlUtils;
import java.util.regex.Pattern;

public class SanitizationUtil {
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i).*(union|select|insert|update|delete|drop|create|alter|exec|script).*"
    );
    
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        String sanitized = HtmlUtils.htmlEscape(input.trim());
        if (SQL_INJECTION_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Invalid input detected");
        }
        return sanitized;
    }
    
    public static String sanitizeForDatabase(String input) {
        if (input == null) return null;
        String cleaned = input.trim().replaceAll("[<>\"'%;()&+]", "");
        if (SQL_INJECTION_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Invalid input detected");
        }
        return cleaned;
    }
    
    public static boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty() && 
               !SQL_INJECTION_PATTERN.matcher(input).matches();
    }
}