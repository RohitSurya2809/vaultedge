package com.rohitsurya2809.vaultedge.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountResponse {
    private UUID id;
    private CustomerSummary customer;
    private String accountNumber;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private String status;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
