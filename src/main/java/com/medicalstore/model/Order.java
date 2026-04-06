package com.medicalstore.model;

import jakarta.persistence.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "order_details")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    private LocalDateTime orderDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal finalAmountAfterDiscount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaidByCustomer = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    
    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }
    
    public enum OrderStatus {
        PENDING, CONFIRMED, READY, DELIVERED, CANCELLED
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    
    public BigDecimal getFinalAmountAfterDiscount() { return finalAmountAfterDiscount; }
    public void setFinalAmountAfterDiscount(BigDecimal finalAmountAfterDiscount) { this.finalAmountAfterDiscount = finalAmountAfterDiscount; }
    
    public BigDecimal getFinalAmount() { return finalAmountAfterDiscount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmountAfterDiscount = finalAmount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public BigDecimal getAmountPaidByCustomer() { return amountPaidByCustomer; }
    public void setAmountPaidByCustomer(BigDecimal amountPaidByCustomer) { this.amountPaidByCustomer = amountPaidByCustomer; }
}