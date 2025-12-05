package com.rohitsurya2809.vaultedge.controller;


import com.rohitsurya2809.vaultedge.dto.CustomerResponse;
import com.rohitsurya2809.vaultedge.dto.LoginRequest;
import com.rohitsurya2809.vaultedge.dto.LoginResponse;
import com.rohitsurya2809.vaultedge.dto.RegisterRequest;
import com.rohitsurya2809.vaultedge.model.Customer;
import com.rohitsurya2809.vaultedge.service.AuthService;
import com.rohitsurya2809.vaultedge.service.CustomerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register: creates both Customer and AuthUser.
     * Returns created Customer (you can adjust to return DTO).
     */
    @PostMapping("/register")
    public ResponseEntity<Customer> register(@RequestBody RegisterRequest req) {
        Customer saved = authService.register(req);
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * Login: returns JWT token.
     */
    
    @PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    LoginResponse resp = authService.login(req);
    return ResponseEntity.ok(resp);
}
}