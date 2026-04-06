package com.medicalstore.controller;

import com.medicalstore.model.UserDetails;
import com.medicalstore.service.*;
import com.medicalstore.util.Constants;
import com.medicalstore.util.SessionUtil;
import com.medicalstore.util.ValidationUtil;
import com.medicalstore.util.SanitizationUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserDetailService userDetailService;
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private StockDetailsService stockDetailsService;
    
    @Autowired
    private AuditService auditService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        SessionUtil.addUserToModel(model, session);
        var stats = dashboardService.getDashboardStats();
        model.addAllAttributes(stats);
        model.addAttribute("lowStockItems", stockDetailsService.getLowStockItems());
        model.addAttribute("hasAlerts", stockDetailsService.hasLowStockAlerts());
        return "dashboard";
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        if (SessionUtil.isLoggedIn(session)) return "redirect:/dashboard";
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            username = SanitizationUtil.sanitizeInput(username);
            ValidationUtil.validateLogin(username, password);
            var u = userDetailService.login(username, password);
            if (u == null) {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
            session.setAttribute(Constants.USER_SESSION_KEY, u);
            session.setAttribute(Constants.USER_ROLE_KEY, u.getRole());
            auditService.logLogin(username);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String email,
                           Model model) {
        try {
            username = SanitizationUtil.sanitizeInput(username);
            fullName = SanitizationUtil.sanitizeInput(fullName);
            email = SanitizationUtil.sanitizeInput(email);
            ValidationUtil.validateRegistration(username, password, fullName, email);
            UserDetails u = new UserDetails(username,password,fullName,email);
            boolean userDetail = userDetailService.register(u);
            if (!userDetail) {
                model.addAttribute("error","Username already exists");
                return "register";
            }
            model.addAttribute("msg","Registered successfully. You can login now.");
            return "login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard/monthly-sales")
    @ResponseBody
    public java.util.List<Double> getMonthlySales() {
        return dashboardService.getMonthlySalesData();
    }

    @GetMapping("/dashboard/stock-distribution")
    @ResponseBody
    public java.util.Map<String, Long> getStockDistribution() {
        return dashboardService.getStockDistributionData();
    }

    @GetMapping("/dashboard/pending-orders")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> getPendingOrders() {
        return dashboardService.getPendingOrders();
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
