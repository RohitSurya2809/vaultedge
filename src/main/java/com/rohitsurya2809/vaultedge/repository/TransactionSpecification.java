package com.rohitsurya2809.vaultedge.repository;

import com.rohitsurya2809.vaultedge.model.Transaction;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> forAccount(UUID accountId) {
        return (root, query, cb) -> cb.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transaction> withType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) return null;
            return cb.equal(cb.upper(root.get("type")), type.trim().toUpperCase());
        };
    }

    public static Specification<Transaction> fromDate(OffsetDateTime from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Transaction> toDate(OffsetDateTime to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    // Combine helper
    public static Specification<Transaction> build(UUID accountId, String type, OffsetDateTime from, OffsetDateTime to) {
        Specification<Transaction> spec = Specification.where(forAccount(accountId));
        if (type != null && !type.isBlank()) spec = spec.and(withType(type));
        if (from != null) spec = spec.and(fromDate(from));
        if (to != null) spec = spec.and(toDate(to));
        return spec;
    }
}
