package com.rohitsurya2809.vaultedge.dto;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerSummary {
    private UUID id;
    private String fullName;
    private String email;
}
