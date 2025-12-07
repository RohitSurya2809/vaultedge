package com.rohitsurya2809.vaultedge.controller;

import com.rohitsurya2809.vaultedge.model.Account;
import com.rohitsurya2809.vaultedge.service.AccountService;
import com.rohitsurya2809.vaultedge.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rohitsurya2809.vaultedge.dto.AccountResponse;
import com.rohitsurya2809.vaultedge.dto.CustomerSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AccountController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    // Create account for the logged-in user: token must contain uid
    @PostMapping
public ResponseEntity<AccountResponse> createAccount(@RequestHeader("Authorization") String authHeader,
                                                     @RequestBody CreateAccountRequest req) {
    String token = extractBearer(authHeader);
    UUID userId = jwtUtil.extractUserId(token);
    Account acc = accountService.createAccount(userId, req.getAccountType(), req.getCurrency(), req.getInitialDeposit());
    return ResponseEntity.status(201).body(toResponse(acc));
}

    @GetMapping("/{id}")
public ResponseEntity<AccountResponse> getAccount(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(toResponse(accountService.getAccount(id)));
}

    @GetMapping
public ResponseEntity<List<AccountResponse>> listAccountsForCustomer(@RequestHeader("Authorization") String authHeader) {
    String token = extractBearer(authHeader);
    UUID userId = jwtUtil.extractUserId(token);
    List<Account> accounts = accountService.listAccountsByCustomer(userId);
    List<AccountResponse> resp = accounts.stream().map(this::toResponse).toList();
    return ResponseEntity.ok(resp);
}

    private String extractBearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) throw new IllegalArgumentException("Missing Bearer token");
        return header.substring(7);
    }

    // DTO for account creation
    public static class CreateAccountRequest {
        private String accountType;
        private String currency;
        private BigDecimal initialDeposit;

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getInitialDeposit() { return initialDeposit; }
        public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }
    }
    private AccountResponse toResponse(Account acc) {
    CustomerSummary cs = CustomerSummary.builder()
            .id(acc.getCustomer().getId())
            .fullName(acc.getCustomer().getFullName())
            .email(acc.getCustomer().getEmail())
            .build();

    return AccountResponse.builder()
            .id(acc.getId())
            .customer(cs)
            .accountNumber(acc.getAccountNumber())
            .accountType(acc.getAccountType())
            .currency(acc.getCurrency())
            .balance(acc.getBalance())
            .status(acc.getStatus())
            .version(acc.getVersion())
            .createdAt(acc.getCreatedAt())
            .updatedAt(acc.getUpdatedAt())
            .build();
}
}
