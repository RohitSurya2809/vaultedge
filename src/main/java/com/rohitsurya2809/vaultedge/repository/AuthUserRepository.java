package com.rohitsurya2809.vaultedge.repository;

import com.rohitsurya2809.vaultedge.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByUsername(String username);
    Optional<AuthUser> findByCustomerId(UUID customerId);
}
