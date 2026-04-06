package com.medicalstore.repo;

import com.medicalstore.model.LatestStocksDetails;
import com.medicalstore.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LatestStocksDetailsRepository extends JpaRepository<LatestStocksDetails, Long> {

    List<LatestStocksDetails> findByMedicineMedicineNameContainingIgnoreCase(String name);

    @Query("SELECT i FROM LatestStocksDetails i JOIN FETCH i.medicine WHERE i.createdDate BETWEEN :startDate AND :endDate")
    List<LatestStocksDetails> findByCreatedDateBetween(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // Dashboard queries
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM LatestStocksDetails i")
    Long sumQuantity();
    
    Long countByQuantityLessThan(int quantity);

    // Stock alert queries
    @Query("SELECT s FROM LatestStocksDetails s JOIN FETCH s.medicine WHERE s.quantity < :threshold")
    List<LatestStocksDetails> findByQuantityLessThan(@Param("threshold") int threshold);

    @Query("SELECT s FROM LatestStocksDetails s JOIN FETCH s.medicine WHERE s.createdDate BETWEEN :startDate AND :endDate")
    List<LatestStocksDetails> findAllByCreatedDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM LatestStocksDetails s JOIN FETCH s.medicine WHERE LOWER(s.medicine.medicineName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<LatestStocksDetails> findByMedicine_MedicineNameContainingIgnoreCase(@Param("query") String query);

    @Query("SELECT s FROM LatestStocksDetails s JOIN FETCH s.medicine WHERE LOWER(s.medicine.medicineName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.medicine.genericName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<LatestStocksDetails> searchByNameOrGeneric(@Param("query") String query);

    List<LatestStocksDetails> findByMedicine(Medicine medicine);

    @Query("SELECT s FROM LatestStocksDetails s JOIN FETCH s.medicine")
    Page<LatestStocksDetails> findAllWithMedicine(Pageable pageable);

    @Query("SELECT s FROM LatestStocksDetails s JOIN FETCH s.medicine")
    List<LatestStocksDetails> findAllWithMedicine();
}
