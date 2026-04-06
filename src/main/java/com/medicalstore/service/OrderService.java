package com.medicalstore.service;

import com.medicalstore.model.Customer;
import com.medicalstore.model.LatestStocksDetails;
import com.medicalstore.model.Order;
import com.medicalstore.model.OrderItem;
import com.medicalstore.repo.LatestStocksDetailsRepository;
import com.medicalstore.repo.OrderRepository;
import com.medicalstore.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private LatestStocksDetailsRepository latestStocksDetailsRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Transactional
    public Order createOrder(Long customerId, Map<Long, Integer> itemQuantities, BigDecimal discount) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (itemQuantities == null || itemQuantities.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        Order order = new Order();
        order.setCustomer(customer);
        order.setDiscount(discount);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            Long inventoryId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            
            LatestStocksDetails stock = latestStocksDetailsRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found in inventory"));
            
            if (stock.getQuantity() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for " + stock.getMedicine().getMedicineName()
                    + " (available: " + stock.getQuantity() + ", requested: " + quantity + ")");
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setInventory(stock);
            orderItem.setQuantity(quantity);
            orderItem.setMedicine(stock.getMedicine());

            BigDecimal mrp = stock.getMaxRetailPrice();
            if (mrp == null || mrp.compareTo(BigDecimal.ZERO) <= 0) {
                mrp = stock.getPurchaseRate().multiply(new BigDecimal("1.20")).setScale(2, RoundingMode.HALF_UP);
            }
            orderItem.setMaxRetailPrice(mrp);
            orderItem.setTotalAmount(mrp.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
            orderItems.add(orderItem);
            
            stock.setQuantity(stock.getQuantity() - quantity);
            latestStocksDetailsRepository.save(stock);
            
            totalAmount = totalAmount.add(orderItem.getTotalAmount());
        }
        
        order.setTotalAmount(totalAmount);
        order.setFinalAmountAfterDiscount(totalAmount.subtract(discount).setScale(2, RoundingMode.HALF_UP));
        order.setAmountPaidByCustomer(BigDecimal.ZERO);
        order.setOrderItems(orderItems);
        
        Order saved = orderRepository.save(order);
        return orderRepository.findByIdWithItems(saved.getId()).orElse(saved);
    }
    
    public List<LatestStocksDetails> searchAvailableMedicines(String query) {
        try {
            return latestStocksDetailsRepository.searchByNameOrGeneric(query)
                .stream()
                .filter(inv -> inv.getQuantity() > 0)
                .limit(10)
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithItems();
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatusWithItems(status);
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findByIdWithItems(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
    }
    
    public void updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }
    
    public void updatePayment(Long orderId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setAmountPaidByCustomer(order.getAmountPaidByCustomer().add(amount));
        orderRepository.save(order);
    }
    
    public List<Order> searchOrdersByCustomer(String customerName) {
        return orderRepository.findByCustomerNameWithItems(customerName);
    }
    
    public List<Order> getOrdersByDateRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return orderRepository.findByOrderDateBetween(start, end);
    }
}
