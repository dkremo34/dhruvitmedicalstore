package com.medicalstore.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item_details")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "latest_stock_detail_id")
    private LatestStocksDetails latestStocksDetails;
    
    private int quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxRetailPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;
    
    public OrderItem() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public LatestStocksDetails getLatestStocksDetails() { return latestStocksDetails; }
    public void setLatestStocksDetails(LatestStocksDetails latestStocksDetails) { this.latestStocksDetails = latestStocksDetails; }
    
    public LatestStocksDetails getInventory() { return latestStocksDetails; }
    public void setInventory(LatestStocksDetails latestStocksDetails) { this.latestStocksDetails = latestStocksDetails; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getMaxRetailPrice() { return maxRetailPrice; }
    public void setMaxRetailPrice(BigDecimal maxRetailPrice) { this.maxRetailPrice = maxRetailPrice; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getTotalPrice() { return totalAmount; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalAmount = totalPrice; }
    
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
}