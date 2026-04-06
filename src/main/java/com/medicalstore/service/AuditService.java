package com.medicalstore.service;

import com.medicalstore.model.AuditTrail;
import com.medicalstore.repo.AuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditTrailRepository auditRepository;

    public void logAction(String action, String entityType, Long entityId, String username, String details) {
        AuditTrail audit = new AuditTrail(action, entityType, entityId, username, details);
        auditRepository.save(audit);
    }

    public void logLogin(String username) {
        logAction("LOGIN", "USER", null, username, "User logged in");
    }

    public void logMedicineAdd(Long medicineId, String username, String medicineName) {
        logAction("CREATE", "MEDICINE", medicineId, username, "Added medicine: " + medicineName);
    }

    public void logInventoryUpdate(Long inventoryId, String username, String details) {
        logAction("UPDATE", "INVENTORY", inventoryId, username, details);
    }
}