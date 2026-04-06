package com.medicalstore.model;

import java.time.LocalDate;

public class PurchaseRequest {

    private Long medicineId;
    private String medicineName;
    private String batchNo;
    private int quantity;
    private double sellingPrice;
    private double maxRetailPrice;
    private LocalDate expiryDate;
    private String dealerName;

    public PurchaseRequest() {
    }


    public PurchaseRequest(Long medicineId, String medicineName, String batchNo, int quantity,
                           double sellingPrice, double maxRetailPrice, LocalDate expiryDate, String dealerName) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.maxRetailPrice = maxRetailPrice;
        this.expiryDate = expiryDate;
        this.dealerName = dealerName;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getMaxRetailPrice() {
        return maxRetailPrice;
    }

    public void setMaxRetailPrice(double maxRetailPrice) {
        this.maxRetailPrice = maxRetailPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }
}
