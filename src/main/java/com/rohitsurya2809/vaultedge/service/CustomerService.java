package com.rohitsurya2809.vaultedge.service;

import com.rohitsurya2809.vaultedge.dto.RegisterRequest;
import com.rohitsurya2809.vaultedge.dto.CustomerResponse;
import com.rohitsurya2809.vaultedge.model.Customer;
import com.rohitsurya2809.vaultedge.repository.CustomerRepository;
import com.rohitsurya2809.vaultedge.exception.BadRequestException;
import com.rohitsurya2809.vaultedge.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository repo;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer register(RegisterRequest req) {

    Optional<Customer> existing = repo.findByEmail(req.getEmail());
    if (existing.isPresent()) {
        throw new BadRequestException("Email already exists");
    }

    Customer c = Customer.builder()
            .fullName(req.getFullName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .address(req.getAddress())
            .build();

    return repo.save(c);
}


    public Customer getById(String id) {
        return repo.findById(java.util.UUID.fromString(id)).orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    public List<Customer> listAll() {
        return repo.findAll();
    }

    public Customer update(String id, RegisterRequest req) {
        Customer c = getById(id);
        c.setFullName(req.getFullName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        // do NOT update email/password here for simplicity (or add separate endpoints)
        return repo.save(c);
    }

    public void delete(String id) {
        Customer c = getById(id);
        repo.delete(c);
    }
}

