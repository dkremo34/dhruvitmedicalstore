package com.medicalstore.service;

import com.medicalstore.repo.LatestStocksDetailsRepository;
import com.medicalstore.repo.MedicineRepository;
import com.medicalstore.repo.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private LatestStocksDetailsRepository latestStocksDetailsRepository;
    
    @Autowired
    private MedicineRepository medicineRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMedicines", medicineRepository.count());
        stats.put("availableStocks", latestStocksDetailsRepository.sumQuantity());
        stats.put("todaySales", getTodayOrdersCount());
        stats.put("pendingPurchase", getPendingOrdersCount());
        return stats;
    }
    
    public java.util.Map<String, Long> getStockDistributionData() {
        return latestStocksDetailsRepository.findAll()
            .stream()
            .collect(java.util.stream.Collectors.groupingBy(
                stock -> stock.getMedicine().getCategory(),
                java.util.stream.Collectors.summingLong(
                    stock -> (long) stock.getQuantity()
                )
            ));
    }
    
    public java.util.List<Double> getMonthlySalesData() {
        java.util.List<Double> monthlySales = new java.util.ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            LocalDateTime startOfMonth = LocalDate.of(LocalDate.now().getYear(), month, 1).atStartOfDay();
            LocalDateTime endOfMonth = startOfMonth.toLocalDate().withDayOfMonth(
                startOfMonth.toLocalDate().lengthOfMonth()).atTime(23, 59, 59);
            
            Double monthlyTotal = orderRepository.findByOrderDateBetween(startOfMonth, endOfMonth)
                .stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(orderItem -> orderItem.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .doubleValue();
            
            monthlySales.add(monthlyTotal);
        }
        
        return monthlySales;
    }
    
    private long getTodayOrdersCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return orderRepository.countByOrderDateBetween(startOfDay, endOfDay);
    }
    
    private long getPendingOrdersCount() {
        return orderRepository.countByStatus(com.medicalstore.model.Order.OrderStatus.PENDING);
    }
    
    public java.util.List<java.util.Map<String, Object>> getPendingOrders() {
        return orderRepository.findByStatus(com.medicalstore.model.Order.OrderStatus.PENDING)
            .stream()
            .map(order -> {
                java.util.Map<String, Object> orderMap = new java.util.HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("customerName", order.getCustomer().getName());
                orderMap.put("customerPhone", order.getCustomer().getPhone());
                orderMap.put("finalAmount", order.getFinalAmount());
                orderMap.put("orderDate", order.getOrderDate());
                return orderMap;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}