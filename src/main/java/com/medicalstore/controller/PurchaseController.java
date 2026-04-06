package com.medicalstore.controller;

import com.medicalstore.model.LatestStocksDetails;
import com.medicalstore.model.Medicine;
import com.medicalstore.model.Purchase;
import com.medicalstore.repo.LatestStocksDetailsRepository;
import com.medicalstore.repo.MedicineRepository;
import com.medicalstore.repo.PurchaseRepository;
import com.medicalstore.util.Constants;
import com.medicalstore.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PurchaseController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    LatestStocksDetailsRepository latestStocksDetailsRepository;

    @GetMapping("/purchase")
    public String purchasePage(Model model, HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        SessionUtil.addUserToModel(model, session);
        return "purchase";
    }

    @GetMapping("/api/medicine/searchWithStock")
    @ResponseBody
    public List<Map<String, Object>> searchMedicineWithStock(@RequestParam String query) {
        List<Medicine> medicines = medicineRepository.findByMedicineNameContainingIgnoreCase(query);
        return medicines.stream().map(m -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", m.getId());
            result.put("medicineName", m.getMedicineName());
            result.put("manufacturer", m.getManufacturer());
            result.put("mrp", m.getMrp());
            List<LatestStocksDetails> stocks = latestStocksDetailsRepository.findByMedicine(m);
            int totalStock = stocks.stream().mapToInt(LatestStocksDetails::getQuantity).sum();
            result.put("currentStock", totalStock);
            return result;
        }).toList();
    }

    @PostMapping("/api/purchases/add")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public String addPurchases(@RequestBody Map<String, Object> purchaseData, HttpSession session) {
        try {
            if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
                return "unauthorized";
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) purchaseData.get("items");
            if (items == null || items.isEmpty()) {
                return "error: No items provided";
            }

            String supplierName = (String) purchaseData.get("supplierName");
            String invoiceNo = (String) purchaseData.get("invoiceNo");
            String paymentMode = (String) purchaseData.get("paymentMode");

            if (supplierName == null || supplierName.trim().isEmpty()) {
                return "error: Supplier name is required";
            }

            for (Map<String, Object> item : items) {
                Long medicineId = Long.valueOf(item.get("medicineId").toString());
                Medicine medicine = medicineRepository.findById(medicineId)
                        .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

                String batchNo = (String) item.get("batchNo");
                if (batchNo == null || batchNo.trim().isEmpty()) {
                    return "error: Batch number is required for all items";
                }

                int quantity = ((Number) item.get("quantity")).intValue();
                if (quantity <= 0) {
                    return "error: Quantity must be greater than zero";
                }
                if (quantity > 100000) {
                    return "error: Quantity exceeds maximum allowed (100000)";
                }

                BigDecimal purchaseRate = new BigDecimal(item.get("purchaseRate").toString());
                BigDecimal mrp = new BigDecimal(item.get("mrp").toString());

                if (purchaseRate.compareTo(BigDecimal.ZERO) <= 0) {
                    return "error: Purchase rate must be greater than zero";
                }
                if (mrp.compareTo(BigDecimal.ZERO) <= 0) {
                    return "error: MRP must be greater than zero";
                }

                String expiryDateStr = (String) item.get("expiryDate");
                if (expiryDateStr == null || expiryDateStr.trim().isEmpty()) {
                    return "error: Expiry date is required";
                }
                LocalDate expiryDate = LocalDate.parse(expiryDateStr);
                if (expiryDate.isBefore(LocalDate.now())) {
                    return "error: Expiry date cannot be in the past";
                }

                Purchase p = new Purchase();
                p.setMedicine(medicine);
                p.setBatchNo(batchNo.trim());
                p.setQuantity(quantity);
                p.setPurchaseRate(purchaseRate);
                p.setMaxRetailPrice(mrp);
                p.setExpiryDate(expiryDate);
                p.setSupplierName(supplierName.trim());
                p.setAmount(purchaseRate.multiply(BigDecimal.valueOf(quantity)));
                p.setInvoiceNo(invoiceNo);
                p.setPaymentMode(paymentMode);
                purchaseRepository.save(p);

                List<LatestStocksDetails> existingStocks = latestStocksDetailsRepository.findByMedicine(medicine);
                
                if (!existingStocks.isEmpty()) {
                    LatestStocksDetails existing = existingStocks.get(0);
                    existing.setQuantity(existing.getQuantity() + quantity);
                    if (purchaseRate.compareTo(existing.getPurchaseRate()) > 0) {
                        existing.setPurchaseRate(purchaseRate);
                    }
                    existing.setMaxRetailPrice(mrp.max(existing.getMaxRetailPrice()));
                    existing.setAmount(existing.getAmount().add(purchaseRate.multiply(BigDecimal.valueOf(quantity))));
                    latestStocksDetailsRepository.save(existing);
                } else {
                    LatestStocksDetails newStock = new LatestStocksDetails();
                    newStock.setMedicine(medicine);
                    newStock.setBatchNo(batchNo.trim());
                    newStock.setQuantity(quantity);
                    newStock.setPurchaseRate(purchaseRate);
                    newStock.setMaxRetailPrice(mrp);
                    newStock.setExpiryDate(expiryDate);
                    newStock.setSupplierName(supplierName.trim());
                    newStock.setAmount(purchaseRate.multiply(BigDecimal.valueOf(quantity)));
                    newStock.setInvoiceNo(invoiceNo);
                    newStock.setPaymentMode(paymentMode);
                    latestStocksDetailsRepository.save(newStock);
                }
            }
            return "success";
        } catch (IllegalArgumentException e) {
            logger.warn("Purchase validation error: {}", e.getMessage());
            return "error: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Error processing purchase", e);
            return "error: Failed to process purchase";
        }
    }
}
