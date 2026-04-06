package com.medicalstore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_trail")
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String action;
    private String entityType;
    private Long entityId;
    private String username;
    private LocalDateTime timestamp;
    private String details;

    public AuditTrail() {}

    public AuditTrail(String action, String entityType, Long entityId, String username, String details) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.username = username;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}