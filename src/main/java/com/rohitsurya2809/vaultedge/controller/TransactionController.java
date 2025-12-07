package com.rohitsurya2809.vaultedge.controller;

import com.rohitsurya2809.vaultedge.dto.*;
import com.rohitsurya2809.vaultedge.service.TransactionService;
import com.rohitsurya2809.vaultedge.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    public TransactionController(TransactionService transactionService, JwtUtil jwtUtil) {
        this.transactionService = transactionService;
        this.jwtUtil = jwtUtil;
    }

    // Deposit into an account (user must own account or admin — ownership check not included here)
    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable("accountId") UUID accountId,
                                                       @RequestBody DepositRequest req,
                                                       @RequestHeader(value = "Authorization", required = false) String auth) {
        TransactionResponse resp = transactionService.deposit(accountId, req);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable("accountId") UUID accountId,
                                                        @RequestBody WithdrawRequest req,
                                                        @RequestHeader(value = "Authorization", required = false) String auth) {
        TransactionResponse resp = transactionService.withdraw(accountId, req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransferRequest req,
                                                        @RequestHeader(value = "Authorization", required = false) String auth) {
        TransactionResponse resp = transactionService.transfer(req);
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<List<TransactionResponse>> listByAccount(@PathVariable("accountId") UUID accountId) {
        return ResponseEntity.ok(transactionService.listForAccount(accountId));
    }
}
