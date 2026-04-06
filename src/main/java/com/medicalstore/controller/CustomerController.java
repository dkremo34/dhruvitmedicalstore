package com.medicalstore.controller;

import com.medicalstore.model.Customer;
import com.medicalstore.repository.CustomerRepository;
import com.medicalstore.util.Constants;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @GetMapping
    public String showCustomerPage(Model model, HttpSession session) {
        Object user = session.getAttribute(Constants.USER_SESSION_KEY);
        if (user != null) {
            model.addAttribute("user", user);
        }
        model.addAttribute("customers", customerRepository.findAll());
        return "customer";
    }
    
    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes, HttpSession session) {
        try {

            if (session.getAttribute(Constants.USER_SESSION_KEY) == null) {
                return "redirect:/customer";
            }
            customerRepository.save(customer);
            redirectAttributes.addFlashAttribute("msg", "Customer saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving customer: " + e.getMessage());
        }
        return "redirect:/customer";
    }
    
    @GetMapping("/search")
    @ResponseBody
    public List<Customer> searchCustomers(@RequestParam String query) {
        return customerRepository.findAll().stream()
            .filter(c -> c.getName().toLowerCase().contains(query.toLowerCase()) || 
                        c.getPhone().contains(query))
            .limit(10)
            .toList();
    }
}
