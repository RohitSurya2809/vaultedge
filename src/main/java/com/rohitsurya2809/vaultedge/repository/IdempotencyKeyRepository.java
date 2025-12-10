package com.rohitsurya2809.vaultedge.repository;

import com.rohitsurya2809.vaultedge.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}
