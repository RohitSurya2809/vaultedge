package com.rohitsurya2809.vaultedge.service;

import com.rohitsurya2809.vaultedge.exception.NotFoundException;
import com.rohitsurya2809.vaultedge.model.Account;
import com.rohitsurya2809.vaultedge.model.Customer;
import com.rohitsurya2809.vaultedge.repository.AccountRepository;
import com.rohitsurya2809.vaultedge.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Account createAccount(UUID customerId, String accountType, String currency, BigDecimal initialDeposit) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));

        Account account = Account.builder()
                .customer(customer)
                .accountNumber(generateAccountNumber())
                .accountType(accountType)
                .currency(currency != null ? currency : "INR")
                .balance(initialDeposit != null ? initialDeposit : BigDecimal.ZERO)
                .status("ACTIVE")
                .build();

        return accountRepository.save(account);
    }

    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
    }

    public List<Account> listAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    private String generateAccountNumber() {
        long ts = System.currentTimeMillis() % 1000000000L;
        int r = (int) (Math.random() * 9000) + 1000;
        return "AE" + ts + r;
    }
}
