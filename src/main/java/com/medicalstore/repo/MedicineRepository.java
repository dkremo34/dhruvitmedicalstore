package com.medicalstore.repo;

import com.medicalstore.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    
    List<Medicine> findByMedicineNameContainingIgnoreCase(String keyword);

    
    @Query("SELECT COUNT(DISTINCT m.category) FROM Medicine m")
    long countDistinctCategories();
    
    @Query("SELECT COUNT(DISTINCT m.manufacturer) FROM Medicine m")
    long countDistinctManufacturers();
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Purchase p WHERE p.medicine.id = :medicineId")
    boolean existsInPurchases(@Param("medicineId") Long medicineId);
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM LatestStocksDetails i WHERE i.medicine.id = :medicineId")
    boolean existsInInventory(@Param("medicineId") Long medicineId);

}

