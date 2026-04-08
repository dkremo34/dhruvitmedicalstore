package com.medicalstore.controller;

import com.medicalstore.model.LatestStocksDetails;
import com.medicalstore.model.Order;
import com.medicalstore.service.StockDetailsService;
import com.medicalstore.service.OrderService;
import com.medicalstore.util.Constants;
import com.medicalstore.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class StocksController {
    @Autowired
    private StockDetailsService stockDetailsService;
    
    @Autowired
    private OrderService orderService;

    @GetMapping("/stocks")
    public String getAllInventory(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return "redirect:/inventory?page=" + page + "&size=" + size;
    }

    @GetMapping("/purchase1")
    public String showPurchasePage(Model model, HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        SessionUtil.addUserToModel(model, session);
        return "purchase";
    }

    @GetMapping("/stocks/today-orders")
    @ResponseBody
    public BigDecimal getTodayOrdersValue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        
        List<Order> todayOrders = orderService.getOrdersByDateRange(startOfDay, endOfDay);
        
        return todayOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(orderItem -> orderItem.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @GetMapping("/running")
    public String home() {
        return "APP RUNNING 🚀";
    }

}
