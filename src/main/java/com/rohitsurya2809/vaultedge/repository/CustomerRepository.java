package com.rohitsurya2809.vaultedge.repository;

import com.rohitsurya2809.vaultedge.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
}
