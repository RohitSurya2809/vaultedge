package com.rohitsurya2809.vaultedge.service;

import com.rohitsurya2809.vaultedge.dto.*;
import com.rohitsurya2809.vaultedge.exception.BadRequestException;
import com.rohitsurya2809.vaultedge.exception.NotFoundException;
import com.rohitsurya2809.vaultedge.model.Account;
import com.rohitsurya2809.vaultedge.model.Transaction;
import com.rohitsurya2809.vaultedge.repository.AccountRepository;
import com.rohitsurya2809.vaultedge.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse deposit(UUID accountId, DepositRequest req) {
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be greater than 0");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        // update balance
        BigDecimal newBalance = account.getBalance().add(req.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account); // optimistic locking ensures version increment

        // create transaction record
        Transaction tx = Transaction.builder()
                .account(account)
                .type("DEPOSIT")
                .amount(req.getAmount())
                .balanceAfter(newBalance)
                .status("COMPLETED")
                .build();

        transactionRepository.save(tx);

        return toResponse(tx);
    }

    @Transactional
    public TransactionResponse withdraw(UUID accountId, WithdrawRequest req) {
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Withdraw amount must be greater than 0");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        if (account.getBalance().compareTo(req.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        BigDecimal newBalance = account.getBalance().subtract(req.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .account(account)
                .type("WITHDRAW")
                .amount(req.getAmount())
                .balanceAfter(newBalance)
                .status("COMPLETED")
                .build();

        transactionRepository.save(tx);

        return toResponse(tx);
    }

    /**
     * Transfer amount from one account to another.
     * If referenceId provided and exists, return existing transaction to make the call idempotent.
     */
    @Transactional
    public TransactionResponse transfer(TransferRequest req) {
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be greater than 0");
        }
        if (req.getFromAccountId().equals(req.getToAccountId())) {
            throw new BadRequestException("From and To accounts must differ");
        }

        // idempotency: if referenceId provided and transaction exists, return it
        if (req.getReferenceId() != null) {
            transactionRepository.findByReferenceId(req.getReferenceId())
                    .ifPresent(existing -> {
                        // return early by throwing a special exception? Instead we'll map and return below
                    });
            if (transactionRepository.findByReferenceId(req.getReferenceId()).isPresent()) {
                Transaction existing = transactionRepository.findByReferenceId(req.getReferenceId()).get();
                return toResponse(existing);
            }
        }

        Account from = accountRepository.findById(req.getFromAccountId())
                .orElseThrow(() -> new NotFoundException("Source account not found: " + req.getFromAccountId()));
        Account to = accountRepository.findById(req.getToAccountId())
                .orElseThrow(() -> new NotFoundException("Destination account not found: " + req.getToAccountId()));

        if (from.getBalance().compareTo(req.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance in source account");
        }

        // debit from source
        BigDecimal fromNew = from.getBalance().subtract(req.getAmount());
        from.setBalance(fromNew);
        accountRepository.save(from);

        // credit to destination
        BigDecimal toNew = to.getBalance().add(req.getAmount());
        to.setBalance(toNew);
        accountRepository.save(to);

        // create two transaction records: TRANSFER_OUT on source, TRANSFER_IN on destination
        Transaction outTx = Transaction.builder()
                .account(from)
                .referenceId(req.getReferenceId())
                .type("TRANSFER_OUT")
                .amount(req.getAmount())
                .balanceAfter(fromNew)
                .status("COMPLETED")
                .build();
        transactionRepository.save(outTx);

        Transaction inTx = Transaction.builder()
                .account(to)
                .referenceId(req.getReferenceId())
                .type("TRANSFER_IN")
                .amount(req.getAmount())
                .balanceAfter(toNew)
                .status("COMPLETED")
                .build();
        transactionRepository.save(inTx);

        // return the outTx as canonical transfer record
        return toResponse(outTx);
    }

    public List<TransactionResponse> listForAccount(UUID accountId) {
        List<Transaction> list = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .accountId(tx.getAccount().getId())
                .referenceId(tx.getReferenceId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalanceAfter())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
