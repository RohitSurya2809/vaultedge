package com.rohitsurya2809.vaultedge.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID id = UUID.randomUUID();

    // link to Customer (one-to-one). keep LAZY to avoid eager fetch.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, columnDefinition = "BINARY(16)")
    private Customer customer;

    @Column(length = 100, nullable = false, unique = true)
    private String username; // may be email

    @Column(nullable = false)
    private String password; // BCrypt hashed

    @Column(length = 255)
    private String roles; // comma separated roles, e.g. "ROLE_USER,ROLE_ADMIN"

    @Column(nullable = false)
    private boolean enabled = true;

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
