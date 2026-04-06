package com.medicalstore.controller;

import com.medicalstore.model.LatestStocksDetails;
import com.medicalstore.service.StockDetailsService;
import com.medicalstore.util.Constants;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;

@Controller
public class InventoryController {

    @Autowired
    StockDetailsService stockDetailsService;

    @GetMapping("/inventory")
    public String showInventory(Model model, HttpSession session,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        var inventoryPage = stockDetailsService.getPaginatedItems(page, size);
        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", inventoryPage.getTotalPages());
        model.addAttribute("user", session.getAttribute("user"));
        return "inventory";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<LatestStocksDetails> searchMedicine(@RequestParam String query) {
        return stockDetailsService.search(query);
    }

    @GetMapping("/medicine/{id}")
    @ResponseBody
    public LatestStocksDetails getMedicine(@PathVariable Long id) {
        return stockDetailsService.getById(id).orElse(null);
    }

    @PostMapping("/inventory/update")
    @ResponseBody
    public ResponseEntity<?> updateMedicine(@RequestBody Map<String, Object> updateData, HttpSession session) {
        // Check session
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return ResponseEntity.status(403).body("Session expired. Please login again.");
        }
        
        try {
            Long id = Long.valueOf(updateData.get("id").toString());
            Optional<LatestStocksDetails> optionalMedicine = stockDetailsService.getById(id);
            
            if (optionalMedicine.isEmpty()) {
                return ResponseEntity.badRequest().body("Medicine not found");
            }
            
            LatestStocksDetails existing = optionalMedicine.get();
            
            // Update fields
            if (updateData.containsKey("batchNo")) {
                existing.setBatchNo(updateData.get("batchNo").toString());
            }
            if (updateData.containsKey("quantity")) {
                existing.setQuantity(Integer.parseInt(updateData.get("quantity").toString()));
            }
            if (updateData.containsKey("maxRetailPrice")) {
                existing.setMaxRetailPrice(new BigDecimal(updateData.get("maxRetailPrice").toString()));
            }
            if (updateData.containsKey("expiryDate")) {
                existing.setExpiryDate(LocalDate.parse(updateData.get("expiryDate").toString()));
            }
            if (updateData.containsKey("supplierName")) {
                existing.setSupplierName(updateData.get("supplierName").toString());
            }
            
            LatestStocksDetails updated = stockDetailsService.updateMedicine(existing);
            return ResponseEntity.ok(updated);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }

    @GetMapping("/inventory/dashboard")
    @ResponseBody
    public java.util.Map<String, Object> getDashboard() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        List<LatestStocksDetails> allItems = stockDetailsService.exportMedicine();
        
        long totalItems = allItems.size();
        long criticalStock = allItems.stream().filter(item -> item.getQuantity() <= 10).count();
        long lowStock = allItems.stream().filter(item -> item.getQuantity() > 20 && item.getQuantity() <= 50).count();
        long expiredItems = allItems.stream().filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now().plusDays(10))).count();
        BigDecimal totalValue = allItems.stream()
            .map(item -> item.getPurchaseRate().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("totalItems", totalItems);
        data.put("criticalStock", criticalStock);
        data.put("lowStock", lowStock);
        data.put("expiredItems", expiredItems);
        data.put("totalValue", String.format("%.2f", totalValue));
        
        return data;
    }

    @GetMapping("/inventory/stock-details")
    @ResponseBody
    public List<Map<String, Object>> getStockDetails(@RequestParam String type) {
        List<LatestStocksDetails> items;
        
        if ("critical".equals(type)) {
            items = stockDetailsService.exportMedicine().stream()
                .filter(item -> item.getQuantity() <= 10)
                .toList();
        } else if ("low".equals(type)) {
            items = stockDetailsService.exportMedicine().stream()
                .filter(item -> item.getQuantity() > 10 && item.getQuantity() <= 50)
                .toList();
        } else if ("expiring".equals(type)) {
            items = stockDetailsService.exportMedicine().stream()
                .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now().plusDays(30)))
                .toList();
        } else {
            items = List.of();
        }
        
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("medicineName", item.getMedicine().getMedicineName());
            map.put("batchNo", item.getBatchNo());
            map.put("quantity", item.getQuantity());
            map.put("maxRetailPrice", item.getMaxRetailPrice());
            map.put("expiryDate", item.getExpiryDate().toString());
            return map;
        }).toList();
    }

    @GetMapping("/exportData")
    public void downloadExcel(@RequestParam("startDate") String startDateStr,
                              @RequestParam("endDate") String endDateStr,
                              HttpServletResponse response) throws IOException {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        List<LatestStocksDetails> latestStocksDetailsList = stockDetailsService.getInventoryByDateRange(startDate, endDate);

        // Create Excel workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Medicine Data");

        // Create header row
        Row header = sheet.createRow(0);
        String[] columns = {"S.NO", "Medicine Name", "Batch No", "Quantity", "Selling Price", "MRP", "Total Amount", "Dealer", "Manufacturer", "Category", "Expiry Date", "Created Date"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }
        // Fill data rows
        int rowIdx = 1;
        double grandTotal = 0.0;
        for (LatestStocksDetails inv : latestStocksDetailsList) {
            Row row = sheet.createRow(rowIdx);
            double totalAmount = inv.getQuantity() * inv.getMaxRetailPrice().doubleValue();
            grandTotal += totalAmount;
            
            row.createCell(0).setCellValue(rowIdx);
            row.createCell(1).setCellValue(inv.getMedicine().getMedicineName());
            row.createCell(2).setCellValue(inv.getBatchNo());
            row.createCell(3).setCellValue(inv.getQuantity());
            row.createCell(4).setCellValue(inv.getPurchaseRate().doubleValue());
            row.createCell(5).setCellValue(inv.getMaxRetailPrice().doubleValue());
            row.createCell(6).setCellValue(totalAmount);
            row.createCell(7).setCellValue(inv.getSupplierName());
            row.createCell(8).setCellValue(inv.getMedicine().getManufacturer());
            row.createCell(9).setCellValue(inv.getMedicine().getCategory());
            row.createCell(10).setCellValue(inv.getExpiryDate().toString());
            row.createCell(11).setCellValue(inv.getCreatedDate().toString());
            rowIdx++;
        }
        
        // Add summary row
        Row summaryRow = sheet.createRow(rowIdx + 1);
        summaryRow.createCell(0).setCellValue("TOTAL");
        summaryRow.createCell(1).setCellValue("Total Medicines: " + latestStocksDetailsList.size());
        summaryRow.createCell(6).setCellValue("Final Amount: ₹" + grandTotal);
        // Set content type and header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=MedicineInventory.xlsx");

        // Write workbook to response
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
