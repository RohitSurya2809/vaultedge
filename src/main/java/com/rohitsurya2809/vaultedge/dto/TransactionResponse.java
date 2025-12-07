package com.rohitsurya2809.vaultedge.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponse {
    private UUID id;
    private UUID accountId;
    private UUID referenceId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String status;
    private OffsetDateTime createdAt;
}
