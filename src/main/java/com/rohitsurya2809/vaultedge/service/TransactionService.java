package com.rohitsurya2809.vaultedge.service;

import com.rohitsurya2809.vaultedge.dto.*;
import com.rohitsurya2809.vaultedge.exception.NotFoundException;
import com.rohitsurya2809.vaultedge.exception.BadRequestException;
import com.rohitsurya2809.vaultedge.model.Account;
import com.rohitsurya2809.vaultedge.model.Transaction;
import com.rohitsurya2809.vaultedge.repository.AccountRepository;
import com.rohitsurya2809.vaultedge.repository.TransactionRepository;
import com.rohitsurya2809.vaultedge.repository.TransactionSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              IdempotencyService idempotencyService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyService = idempotencyService;
    }

    // ---------- Deposit ----------
    @Transactional
    public TransactionResponse deposit(UUID accountId, DepositRequest req, String idempKey) {
        // idempotency check
        if (idempKey != null) {
            TransactionResponse cached = idempotencyService.getIfExists(idempKey, TransactionResponse.class);
            if (cached != null) return cached;
        }

        BigDecimal amount = req.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be greater than 0");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        BigDecimal newBalance = (account.getBalance() == null ? BigDecimal.ZERO : account.getBalance()).add(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .type("DEPOSIT")
                .amount(amount)
                .balanceAfter(newBalance)
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now())
                .build();

        transactionRepository.save(tx);

        TransactionResponse resp = toResponse(tx);

        if (idempKey != null) idempotencyService.save(idempKey, resp);

        return resp;
    }

    // ---------- Withdraw ----------
    @Transactional
    public TransactionResponse withdraw(UUID accountId, WithdrawRequest req, String idempKey) {
        // idempotency check
        if (idempKey != null) {
            TransactionResponse cached = idempotencyService.getIfExists(idempKey, TransactionResponse.class);
            if (cached != null) return cached;
        }

        BigDecimal amount = req.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Withdraw amount must be greater than 0");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        if (balance.compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        BigDecimal newBalance = balance.subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .type("WITHDRAW")
                .amount(amount)
                .balanceAfter(newBalance)
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now())
                .build();

        transactionRepository.save(tx);

        TransactionResponse resp = toResponse(tx);

        if (idempKey != null) idempotencyService.save(idempKey, resp);

        return resp;
    }

    // ---------- Transfer ----------
    @Transactional
    public TransactionResponse transfer(TransferRequest req, String idempKey) {
        // idempotency check
        if (idempKey != null) {
            TransactionResponse cached = idempotencyService.getIfExists(idempKey, TransactionResponse.class);
            if (cached != null) return cached;
        }

        if (req.getFromAccountId() == null || req.getToAccountId() == null) {
            throw new BadRequestException("Both fromAccountId and toAccountId are required");
        }
        if (req.getFromAccountId().equals(req.getToAccountId())) {
            throw new BadRequestException("From and To accounts must differ");
        }

        BigDecimal amount = req.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be greater than 0");
        }

        Account from = accountRepository.findById(req.getFromAccountId())
                .orElseThrow(() -> new NotFoundException("Source account not found: " + req.getFromAccountId()));
        Account to = accountRepository.findById(req.getToAccountId())
                .orElseThrow(() -> new NotFoundException("Destination account not found: " + req.getToAccountId()));

        BigDecimal fromBal = from.getBalance() == null ? BigDecimal.ZERO : from.getBalance();
        if (fromBal.compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance in source account");
        }

        // debit source
        BigDecimal fromNew = fromBal.subtract(amount);
        from.setBalance(fromNew);
        accountRepository.save(from);

        // credit destination
        BigDecimal toBal = to.getBalance() == null ? BigDecimal.ZERO : to.getBalance();
        BigDecimal toNew = toBal.add(amount);
        to.setBalance(toNew);
        accountRepository.save(to);

        // create two transaction records (transfer out + transfer in)
        Transaction outTx = Transaction.builder()
                .id(UUID.randomUUID())
                .account(from)
                .referenceId(req.getReferenceId()) // may be null
                .type("TRANSFER_OUT")
                .amount(amount)
                .balanceAfter(fromNew)
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now())
                .build();
        transactionRepository.save(outTx);

        Transaction inTx = Transaction.builder()
                .id(UUID.randomUUID())
                .account(to)
                .referenceId(req.getReferenceId())
                .type("TRANSFER_IN")
                .amount(amount)
                .balanceAfter(toNew)
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now())
                .build();
        transactionRepository.save(inTx);

        TransactionResponse resp = toResponse(outTx);

        if (idempKey != null) idempotencyService.save(idempKey, resp);

        return resp;
    }

    // ---------- List ----------
    public List<TransactionResponse> listForAccount(UUID accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<TransactionResponse> listForAccountPaged(UUID accountId,
                                                    int page,
                                                    int size,
                                                    String sort,
                                                    String type,
                                                    String fromIso,
                                                    String toIso) {
    // Parse sort param (e.g. "createdAt,desc" or "amount,asc")
    Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); // default
    if (sort != null && !sort.isBlank()) {
        String[] parts = sort.split(",");
        if (parts.length == 2) {
            try {
                sortObj = Sort.by(Sort.Direction.fromString(parts[1].trim()), parts[0].trim());
            } catch (IllegalArgumentException e) {
                sortObj = Sort.by(parts[0].trim());
            }
        } else {
            sortObj = Sort.by(sort.trim());
        }
    }

    Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sortObj);

    OffsetDateTime from = null;
    OffsetDateTime to = null;
    try {
        if (fromIso != null && !fromIso.isBlank()) from = OffsetDateTime.parse(fromIso);
    } catch (DateTimeParseException ignored) {}
    try {
        if (toIso != null && !toIso.isBlank()) to = OffsetDateTime.parse(toIso);
    } catch (DateTimeParseException ignored) {}

    Specification<Transaction> spec = TransactionSpecification.build(accountId, type, from, to);

    Page<Transaction> txPage = transactionRepository.findAll(spec, pageable);

    return txPage.map(this::toResponse);
}

    // ---------- Mapper ----------
    private TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .accountId(tx.getAccount() != null ? tx.getAccount().getId() : null)
                .referenceId(tx.getReferenceId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalanceAfter())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .build();
    }
    public TransactionSummaryResponse getSummary(UUID accountId, String fromIso, String toIso) {
    // fetch all transactions for account (use existing repo convenience method)
    List<Transaction> all = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);

    // parse date filters (null safe)
    OffsetDateTime from = null;
    OffsetDateTime to = null;
    try {
        if (fromIso != null && !fromIso.isBlank()) from = OffsetDateTime.parse(fromIso);
    } catch (DateTimeParseException ignored) {}
    try {
        if (toIso != null && !toIso.isBlank()) to = OffsetDateTime.parse(toIso);
    } catch (DateTimeParseException ignored) {}

    // filter by date range if provided
    final OffsetDateTime finalFrom = from;
    final OffsetDateTime finalTo = to;
    List<Transaction> filtered = all.stream()
            .filter(tx -> {
                OffsetDateTime created = tx.getCreatedAt();
                if (created == null) return false;
                if (finalFrom != null && created.isBefore(finalFrom)) return false;
                if (finalTo != null && created.isAfter(finalTo)) return false;
                return true;
            })
            .collect(Collectors.toList());

    BigDecimal totalDeposits = BigDecimal.ZERO;
    BigDecimal totalWithdrawals = BigDecimal.ZERO;
    Map<String, Long> byType = new HashMap<>();

    for (Transaction tx : filtered) {
        // amount handling: Transaction.amount may be BigDecimal or Double; handle both
        BigDecimal amt;
        Object raw = tx.getAmount();
        if (raw instanceof BigDecimal) {
            amt = (BigDecimal) raw;
        } else if (raw instanceof Number) {
            amt = BigDecimal.valueOf(((Number) raw).doubleValue());
        } else {
            // fallback: try toString
            try {
                amt = new BigDecimal(String.valueOf(raw));
            } catch (Exception e) {
                amt = BigDecimal.ZERO;
            }
        }

        String type = tx.getType() != null ? tx.getType().toUpperCase() : "UNKNOWN";
        byType.put(type, byType.getOrDefault(type, 0L) + 1L);

        if ("DEPOSIT".equals(type) || "TRANSFER_IN".equals(type)) {
            totalDeposits = totalDeposits.add(amt);
        } else if ("WITHDRAW".equals(type) || "TRANSFER_OUT".equals(type)) {
            totalWithdrawals = totalWithdrawals.add(amt);
        } else if ("TRANSFER".equals(type)) {
            // if you use "TRANSFER" as single type, decide if it's debit or credit;
            // we assume transfer recorded on source as TRANSFER or TRANSFER_OUT and on dest as TRANSFER_IN.
        }
    }

    BigDecimal netFlow = totalDeposits.subtract(totalWithdrawals);
    long count = filtered.size();

    return new TransactionSummaryResponse(accountId, totalDeposits, totalWithdrawals, netFlow, count, byType);
}
}
