package com.medicalstore.service;


import com.medicalstore.model.Medicine;
import com.medicalstore.repo.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineService {

    @Autowired
    private MedicineRepository repo;

    public boolean saveMedicine(Medicine medicine) {
        // Check if medicine already exists by name and manufacturer
        List<Medicine>  singleMedicine = repo.findByMedicineNameContainingIgnoreCase(
                medicine.getMedicineName());
        if (singleMedicine == null || singleMedicine.isEmpty()) {        
            repo.save(medicine);
            return false;
        } else {
            Medicine existing = getMedicine(medicine, singleMedicine);
            repo.save(existing);
            return true;
        } 
    }

    private static Medicine getMedicine(Medicine medicine, List<Medicine> singleMedicine) {
        Medicine existing = singleMedicine.get(0);
        // Update existing medicine with new data
        existing.setCategory(medicine.getCategory());
        existing.setGenericName(medicine.getGenericName());
        existing.setStrength(medicine.getStrength());
        existing.setUnit(medicine.getUnit());
        existing.setPackSize(medicine.getPackSize());
        existing.setHsn(medicine.getHsn());
        existing.setMrp(medicine.getMrp());
        existing.setGstRate(medicine.getGstRate());
        existing.setStorageCondition(medicine.getStorageCondition());
        return existing;
    }

    public List<Medicine> findByNameContaining(String query) {
        return repo.findByMedicineNameContainingIgnoreCase(query);
    }
    
    public void deleteMedicine(Long id) {
        repo.deleteById(id);
    }
    
    public long getTotalMedicines() {
        return repo.count();
    }
    
    public long getTotalCategories() {
        return repo.countDistinctCategories();
    }
    
    public long getTotalManufacturers() {
        return repo.countDistinctManufacturers();
    }
    
    public boolean canDeleteMedicine(Long id) {
        // Check if medicine is referenced in purchases or inventory
        return !repo.existsInPurchases(id) && !repo.existsInInventory(id);
    }
}