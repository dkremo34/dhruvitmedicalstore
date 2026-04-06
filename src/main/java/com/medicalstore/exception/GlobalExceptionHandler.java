package com.medicalstore.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        logger.error("Unexpected error occurred", e);
        model.addAttribute("error", "An unexpected error occurred. Please try again.");
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleValidation(IllegalArgumentException e, Model model) {
        logger.warn("Validation error: {}", e.getMessage());
        model.addAttribute("error", "Invalid input: " + e.getMessage());
        return "error";
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationErrors(MethodArgumentNotValidException e, Model model) {
        logger.warn("Validation failed: {}", e.getMessage());
        model.addAttribute("error", "Please check your input and try again.");
        return "error";
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException e, Model model) {
        logger.error("Data integrity violation", e);
        model.addAttribute("error", "Data conflict occurred. Please check for duplicates.");
        return "error";
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleResourceNotFound(NoResourceFoundException e, Model model) {
        logger.debug("Resource not found: {}", e.getResourcePath());
        return "error";
    }
}