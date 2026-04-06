package com.medicalstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "medicine", indexes = {
    @Index(name = "idx_medicine_name", columnList = "medicineName"),
    @Index(name = "idx_manufacturer", columnList = "manufacturer"),
    @Index(name = "idx_category", columnList = "category")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Medicine extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Medicine name is required")
    @Size(min = 2, max = 100, message = "Medicine name must be between 2 and 100 characters")
    private String medicineName;

    @NotBlank(message = "Manufacturer is required")
    @Size(min = 2, max = 100, message = "Manufacturer must be between 2 and 100 characters")
    private String manufacturer;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    private String category;
    private String genericName;
    private String strength;
    private String unit;
    private String packSize;
    private String hsn;

    @Column(precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(precision = 5, scale = 2)
    private BigDecimal gstRate;

    private String storageCondition;

    public Medicine() {
    }

    public Medicine(Long id, String medicineName, String manufacturer, String category, String genericName, String strength, String unit, String packSize, String hsn, BigDecimal mrp, BigDecimal gstRate, String storageCondition) {
        this.id = id;
        this.medicineName = medicineName;
        this.manufacturer = manufacturer;
        this.category = category;
        this.genericName = genericName;
        this.strength = strength;
        this.unit = unit;
        this.packSize = packSize;
        this.hsn = hsn;
        this.mrp = mrp;
        this.gstRate = gstRate;
        this.storageCondition = storageCondition;
    }

    public Medicine(LocalDate createdDate, String updatedBy, String createdBy, LocalDate updatedDate, Long id, String medicineName, String manufacturer, String category, String genericName, String strength, String unit, String packSize, String hsn, BigDecimal mrp, BigDecimal gstRate, String storageCondition) {
        super(createdDate, updatedBy, createdBy, updatedDate);
        this.id = id;
        this.medicineName = medicineName;
        this.manufacturer = manufacturer;
        this.category = category;
        this.genericName = genericName;
        this.strength = strength;
        this.unit = unit;
        this.packSize = packSize;
        this.hsn = hsn;
        this.mrp = mrp;
        this.gstRate = gstRate;
        this.storageCondition = storageCondition;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPackSize() {
        return packSize;
    }

    public void setPackSize(String packSize) {
        this.packSize = packSize;
    }

    public String getHsn() {
        return hsn;
    }

    public void setHsn(String hsn) {
        this.hsn = hsn;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getGstRate() {
        return gstRate;
    }

    public void setGstRate(BigDecimal gstRate) {
        this.gstRate = gstRate;
    }

    public String getStorageCondition() {
        return storageCondition;
    }

    public void setStorageCondition(String storageCondition) {
        this.storageCondition = storageCondition;
    }
}
