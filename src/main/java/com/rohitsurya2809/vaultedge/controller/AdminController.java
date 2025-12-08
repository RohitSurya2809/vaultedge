package com.rohitsurya2809.vaultedge.controller;

import com.rohitsurya2809.vaultedge.model.Customer;
import com.rohitsurya2809.vaultedge.repository.CustomerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final CustomerRepository customerRepository;

    public AdminController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Customer> listAllCustomers() {
        return customerRepository.findAll();
    }
}
