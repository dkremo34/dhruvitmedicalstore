package com.medicalstore.controller;

import com.medicalstore.model.Order;
import com.medicalstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/create")
    public String showOrderForm(Model model, HttpSession session) {
        model.addAttribute("order", new Order());
        model.addAttribute("user", session.getAttribute("user"));
        return "orders/create";
    }
    
    @GetMapping("/search")
    @ResponseBody
    public List<Map<String, Object>> searchMedicines(@RequestParam String query) {
        if (query == null || query.trim().isEmpty() || query.length() < 2) {
            return List.of();
        }
        try {
            return orderService.searchAvailableMedicines(query.trim())
                .stream()
                .map(item -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", item.getId());
                    result.put("quantity", item.getQuantity());
                    result.put("maxRetailPrice", item.getMaxRetailPrice());
                    result.put("purchaseRate", item.getPurchaseRate());
                    result.put("medicineName", item.getMedicine().getMedicineName());
                    result.put("manufacturer", item.getMedicine().getManufacturer());
                    result.put("genericName", item.getMedicine().getGenericName());
                    result.put("expiryDate", item.getExpiryDate() != null ? item.getExpiryDate().toString() : null);
                    return result;
                })
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    @PostMapping("/process")
    public String processOrder(@RequestParam(required = false) Long customerId,
                             @RequestParam(required = false) String customerName,
                             @RequestParam(required = false) String customerPhone,
                             @RequestParam(required = false) String customerAddress,
                             @RequestParam(required = false) BigDecimal discount,
                             @RequestParam Map<String, String> params,
                             Model model, HttpSession session) {
        try {
            if (customerId == null) {
                throw new IllegalArgumentException("Please select a customer");
            }
            if (discount != null && discount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Discount cannot be negative");
            }

            Map<Long, Integer> itemQuantities = parseItemQuantities(params);

            Order processedOrder = orderService.createOrder(customerId, itemQuantities,
                    discount != null ? discount : BigDecimal.ZERO);
            model.addAttribute("order", processedOrder);
            model.addAttribute("user", session.getAttribute("user"));
            return "orders/confirmation";

        } catch (IllegalArgumentException e) {
            logger.warn("Order validation error: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", session.getAttribute("user"));
            return "orders/create";
        } catch (Exception e) {
            logger.error("Error processing order", e);
            model.addAttribute("error", "An error occurred while processing the order");
            model.addAttribute("user", session.getAttribute("user"));
            return "orders/create";
        }
    }

    private static Map<Long, Integer> parseItemQuantities(Map<String, String> params) {
        Map<Long, Integer> itemQuantities = new HashMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("qty_")) {
                try {
                    Long inventoryId = Long.parseLong(entry.getKey().substring(4));
                    int quantity = Integer.parseInt(entry.getValue());
                    if (quantity <= 0) {
                        throw new IllegalArgumentException("Quantity must be greater than zero");
                    }
                    if (quantity > 10000) {
                        throw new IllegalArgumentException("Quantity exceeds maximum allowed (10000)");
                    }
                    itemQuantities.put(inventoryId, quantity);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid quantity format");
                }
            }
        }

        if (itemQuantities.isEmpty()) {
            throw new IllegalArgumentException("Please add items to the order");
        }
        return itemQuantities;
    }

    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, Model model, HttpSession session) {
        try {
            Order order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            model.addAttribute("user", session.getAttribute("user"));
            return "orders/view";
        } catch (Exception e) {
            model.addAttribute("error", "Order not found");
            return "redirect:/orders/list";
        }
    }
    
    @GetMapping("/list")
    public String listOrders(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String customer,
                           Model model, HttpSession session) {
        List<Order> orders;
        
        if (customer != null && !customer.trim().isEmpty()) {
            orders = orderService.searchOrdersByCustomer(customer.trim());
        } else if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
                orders = orderService.getOrdersByStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                orders = orderService.getAllOrders();
            }
        } else {
            orders = orderService.getAllOrders();
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("user", session.getAttribute("user"));
        return "orders/list";
    }
    
    @PostMapping("/update-status")
    public String updateOrderStatus(@RequestParam Long orderId, 
                                  @RequestParam Order.OrderStatus status,
                                  Model model) {
        try {
            orderService.updateOrderStatus(orderId, status);
            return "redirect:/orders/list";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update order status: " + e.getMessage());
            return "redirect:/orders/list";
        }
    }
    
    @PostMapping("/update-payment")
    public String updatePayment(@RequestParam Long orderId,
                              @RequestParam BigDecimal amount,
                              Model model) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Payment amount must be greater than zero");
            }
            orderService.updatePayment(orderId, amount);
            return "redirect:/orders/list";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update payment: " + e.getMessage());
            return "redirect:/orders/list";
        }
    }

    @GetMapping("/export")
    public void exportOrders(@RequestParam("startDate") String startDateStr,
                            @RequestParam("endDate") String endDateStr,
                            HttpServletResponse response) throws IOException {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Order> ordersList = orderService.getOrdersByDateRange(startDateTime, endDateTime);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Order Details");

        Row header = sheet.createRow(0);
        String[] columns = {"S.NO", "Medicine Name", "Order ID", "Customer Name", "Phone", "Quantity", "MRP", "Total Amount", "Order Date", "Status"};
        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        int rowIdx = 1;
        int serialNo = 1;
        double grandTotal = 0.0;
        for (Order order : ordersList) {
            for (var orderItem : order.getOrderItems()) {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(serialNo++);
                row.createCell(1).setCellValue(orderItem.getMedicine().getMedicineName());
                row.createCell(2).setCellValue(order.getId());
                row.createCell(3).setCellValue(order.getCustomer().getName());
                row.createCell(4).setCellValue(order.getCustomer().getPhone());
                row.createCell(5).setCellValue(orderItem.getQuantity());
                row.createCell(6).setCellValue(orderItem.getMaxRetailPrice().doubleValue());
                row.createCell(7).setCellValue(orderItem.getTotalAmount().doubleValue());
                row.createCell(8).setCellValue(order.getOrderDate().toString());
                row.createCell(9).setCellValue(order.getStatus().toString());
                grandTotal += orderItem.getTotalAmount().doubleValue();
                rowIdx++;
            }
        }

        Row summaryRow = sheet.createRow(rowIdx + 1);
        summaryRow.createCell(0).setCellValue("TOTAL");
        summaryRow.createCell(2).setCellValue("Total Orders: " + ordersList.size());
        summaryRow.createCell(7).setCellValue("Final Amount: \u20b9" + grandTotal);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=OrderReport.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
