package com.medicalstore.service;

import com.medicalstore.model.LatestStocksDetails;
import com.medicalstore.repo.LatestStocksDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StockDetailsService {

    @Autowired
    private LatestStocksDetailsRepository latestStocksDetailsRepository;

    public Page<LatestStocksDetails> getPaginatedItems(int page, int size) {
        return latestStocksDetailsRepository.findAllWithMedicine(PageRequest.of(page, size));
    }

    public boolean savePurchase(LatestStocksDetails latestStocksDetails) {
        LatestStocksDetails savedLatestStocksDetails = latestStocksDetailsRepository.save(latestStocksDetails);
        return true;
    }

    public List<LatestStocksDetails> findByMedicineNameIgnoreCase(String name) {
        return latestStocksDetailsRepository.findByMedicineMedicineNameContainingIgnoreCase(name);
    }

    public List<LatestStocksDetails> search(String query) {
        return latestStocksDetailsRepository.findByMedicine_MedicineNameContainingIgnoreCase(query);
    }

    public Optional<LatestStocksDetails> getById(Long id) {
        return latestStocksDetailsRepository.findById(id);
    }

    public LatestStocksDetails updateMedicine(LatestStocksDetails latestStocksDetails) {
        return latestStocksDetailsRepository.save(latestStocksDetails);
    }

    public List<LatestStocksDetails> exportMedicine() {
        return  latestStocksDetailsRepository.findAllWithMedicine();
    }

    public List<LatestStocksDetails> getInventoryByDateRange(LocalDate startDate, LocalDate endDate) {
        return latestStocksDetailsRepository.findAllByCreatedDateBetween(startDate, endDate);
    }

    public List<LatestStocksDetails> getLowStockItems(int threshold) {
        return latestStocksDetailsRepository.findByQuantityLessThan(threshold);
    }

    public List<LatestStocksDetails> getLowStockItems() {
        return getLowStockItems(10); // Default threshold
    }

    public boolean hasLowStockAlerts() {
        return latestStocksDetailsRepository.countByQuantityLessThan(10) > 0;
    }
}
