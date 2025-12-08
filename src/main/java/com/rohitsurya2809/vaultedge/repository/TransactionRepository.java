package com.rohitsurya2809.vaultedge.repository;

import com.rohitsurya2809.vaultedge.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    Optional<Transaction> findByReferenceId(UUID referenceId);
}
