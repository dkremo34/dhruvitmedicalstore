package com.medicalstore.repo;

import com.medicalstore.model.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
}