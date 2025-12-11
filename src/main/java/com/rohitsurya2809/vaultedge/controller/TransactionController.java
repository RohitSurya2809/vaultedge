package com.rohitsurya2809.vaultedge.controller;

import com.rohitsurya2809.vaultedge.dto.*;
import com.rohitsurya2809.vaultedge.model.Account;
import com.rohitsurya2809.vaultedge.service.AccountService;
import com.rohitsurya2809.vaultedge.service.TransactionService;
import com.rohitsurya2809.vaultedge.security.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Tag(name = "Transactions", description = "Deposit, withdraw, transfer and transaction history")
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService,
                                 JwtUtil jwtUtil,
                                 AccountService accountService) {
        this.transactionService = transactionService;
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
    }

    // Helper: extract token (throws 401 if missing)
    private String extractBearerOrThrow(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(UNAUTHORIZED, "Missing Authorization header");
        }
        return authHeader.substring(7);
    }

    // Helper: get caller userId from header (throws 401 if invalid token)
    private UUID getCallerId(String authHeader) {
        String token = extractBearerOrThrow(authHeader);
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid or expired token");
        }
        UUID uid = jwtUtil.extractUserId(token);
        if (uid == null) throw new ResponseStatusException(UNAUTHORIZED, "Token missing uid");
        return uid;
    }

    // Deposit: only account owner can deposit (Idempotency-Key supported)
    @Operation(summary = "Deposit to an account", description = "Deposit amount into account. Provide Idempotency-Key header to avoid duplicates.")
    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable("accountId") UUID accountId,
            @RequestBody DepositRequest req,
            @RequestHeader(value = "Authorization", required = true) String auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        UUID caller = getCallerId(auth);
        Account acc = accountService.getAccount(accountId);
        if (!acc.getCustomer().getId().equals(caller)) {
            throw new ResponseStatusException(FORBIDDEN, "Not owner of account");
        }
        TransactionResponse resp = transactionService.deposit(accountId, req, idempotencyKey);
        return ResponseEntity.status(201).body(resp);
    }

    // Withdraw: only owner (Idempotency-Key supported)
    @Operation(summary = "Withdraw from the account", description = "Withdraw amount from account. Provide Idempotency-Key header to avoid duplicates.")
    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable("accountId") UUID accountId,
            @RequestBody WithdrawRequest req,
            @RequestHeader(value = "Authorization", required = true) String auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        UUID caller = getCallerId(auth);
        Account acc = accountService.getAccount(accountId);
        if (!acc.getCustomer().getId().equals(caller)) {
            throw new ResponseStatusException(FORBIDDEN, "Not owner of account");
        }
        TransactionResponse resp = transactionService.withdraw(accountId, req, idempotencyKey);
        return ResponseEntity.ok(resp);
    }

    // Transfer: caller must own the source account (fromAccountId). Idempotency-Key supported.
    @Operation(summary = "Transfer between accounts", description = "Transfer amount from one account to another. Provide Idempotency-Key header to avoid duplicates.")
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestBody TransferRequest req,
            @RequestHeader(value = "Authorization", required = true) String auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        UUID caller = getCallerId(auth);
        // ensure caller owns the source account
        Account source = accountService.getAccount(req.getFromAccountId());
        if (!source.getCustomer().getId().equals(caller)) {
            throw new ResponseStatusException(FORBIDDEN, "Not owner of source account");
        }
        TransactionResponse resp = transactionService.transfer(req, idempotencyKey);
        return ResponseEntity.status(201).body(resp);
    }

    // List all transactions (non-paged) - only owner
    @Operation(summary = "List all transactions for an account", description = "List all transactions for an account.")
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<List<TransactionResponse>> listByAccount(
            @PathVariable("accountId") UUID accountId,
            @RequestHeader(value = "Authorization", required = true) String auth) {

        UUID caller = getCallerId(auth);
        Account acc = accountService.getAccount(accountId);
        if (!acc.getCustomer().getId().equals(caller)) {
            throw new ResponseStatusException(FORBIDDEN, "Not owner of account");
        }
        return ResponseEntity.ok(transactionService.listForAccount(accountId));
    }

    // Paged & filtered: only owner
    @Operation(summary = "List paged transactions for an account", description = "List paged transactions for an account with optional filters: type, date range.")
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<Map<String, Object>> listByAccountPaged(
            @PathVariable("accountId") UUID accountId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "from", required = false) String fromIso,
            @RequestParam(value = "to", required = false) String toIso,
            @RequestHeader(value = "Authorization", required = true) String auth) {

        UUID caller = getCallerId(auth);
        Account acc = accountService.getAccount(accountId);
        if (!acc.getCustomer().getId().equals(caller)) {
            throw new ResponseStatusException(FORBIDDEN, "Not owner of account");
        }

        Page<TransactionResponse> pg = transactionService.listForAccountPaged(accountId, page, size, sort, type, fromIso, toIso);

        Map<String, Object> resp = Map.of(
                "content", pg.getContent(),
                "page", pg.getNumber(),
                "size", pg.getSize(),
                "totalElements", pg.getTotalElements(),
                "totalPages", pg.getTotalPages(),
                "last", pg.isLast()
        );
        return ResponseEntity.ok(resp);
    }

    // Summary endpoint - only owner
    @Operation(summary = "Get transaction summary for an account", description = "Get transaction summary (total deposits, withdrawals) for an account over an optional date range.")
    @GetMapping("/accounts/{accountId}/summary")
    public ResponseEntity<TransactionSummaryResponse> getSummary(
            @PathVariable UUID accountId,
            @RequestParam(value = "from", required = false) String fromIso,
            @RequestParam(value = "to", required = false) String toIso,
            @RequestHeader(value = "Authorization", required = true) String auth) {

        UUID caller = getCallerId(auth);
        Account acc = accountService.getAccount(accountId);

        if (!acc.getCustomer().getId().equals(caller)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of account");
        }

        TransactionSummaryResponse summary = transactionService.getSummary(accountId, fromIso, toIso);
        return ResponseEntity.ok(summary);
    }
}
