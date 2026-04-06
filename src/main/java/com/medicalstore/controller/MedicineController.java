package com.medicalstore.controller;

import com.medicalstore.model.Medicine;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.AuditService;
import com.medicalstore.util.Constants;
import com.medicalstore.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MedicineController {

    @Autowired
    private MedicineService medicineService;
    
    @Autowired
    private AuditService auditService;

    @GetMapping("/medicine")
    public String showPurchasePage(Model model, HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        SessionUtil.addUserToModel(model, session);
        return "medicine";
    }

    @PostMapping("/medicineSave")
    public String saveMedicine(@ModelAttribute Medicine medicine,
                               Model model, HttpSession session) {
        if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        boolean isUpdate =  medicineService.saveMedicine(medicine);

        String message = isUpdate ? "Medicine updated successfully..!!" : "Medicine added successfully..!!";
        model.addAttribute("msg", message);
        SessionUtil.addUserToModel(model, session);

        var user = session.getAttribute("user");
        String username = user != null ? user.toString() : "Unknown";
        auditService.logMedicineAdd(medicine.getId(), username, medicine.getMedicineName());
        return "medicine";
    }


    // Unified search endpoint for medicine objects
    @GetMapping("/api/medicine/searchObjects")
    @ResponseBody
    public List<Medicine> searchMedicineObjects(@RequestParam String query) {
        return medicineService.findByNameContaining(query);
    }
    
    @DeleteMapping("/api/medicine/delete/{id}")
    @ResponseBody
    public String deleteMedicine(@PathVariable Long id, HttpSession session) {
        try {
            if (!medicineService.canDeleteMedicine(id)) {
                return "Cannot delete medicine - already associated with orders or purchases";
            }
            medicineService.deleteMedicine(id);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/medicine/stats")
    @ResponseBody
    public java.util.Map<String, Object> getMedicineStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalMedicines", medicineService.getTotalMedicines());
        stats.put("totalCategories", medicineService.getTotalCategories());
        stats.put("totalManufacturers", medicineService.getTotalManufacturers());
        return stats;
    }
}

