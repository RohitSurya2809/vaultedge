package com.rohitsurya2809.vaultedge.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, columnDefinition = "BINARY(16)")
    private Customer customer;

    @Column(name = "account_number", length = 30, nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "account_type", length = 20, nullable = false)
    private String accountType;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency = "INR";

    @Column(name = "balance", precision = 18, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    @Version
    private Long version;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
