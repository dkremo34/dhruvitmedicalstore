package com.medicalstore.repo;

import com.medicalstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.medicine LEFT JOIN FETCH o.customer WHERE o.orderDate BETWEEN :start AND :end")
    List<Order> findByOrderDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.customer.name LIKE %:customerName%")
    List<Order> findByCustomerNameContainingIgnoreCase(@Param("customerName") String customerName);
    
    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    
    long countByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.medicine LEFT JOIN FETCH o.customer")
    List<Order> findAllWithItems();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.medicine LEFT JOIN FETCH o.customer WHERE o.id = :id")
    java.util.Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.medicine LEFT JOIN FETCH o.customer WHERE o.status = :status")
    List<Order> findByStatusWithItems(@Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.medicine LEFT JOIN FETCH o.customer WHERE o.customer.name LIKE %:name%")
    List<Order> findByCustomerNameWithItems(@Param("name") String name);
}