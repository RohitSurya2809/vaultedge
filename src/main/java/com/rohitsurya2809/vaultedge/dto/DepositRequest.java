package com.rohitsurya2809.vaultedge.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepositRequest {
    private BigDecimal amount;
}
