package com.rohitsurya2809.vaultedge.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record TransactionSummaryResponse(
        UUID accountId,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        BigDecimal netFlow,
        long count,
        Map<String, Long> byType
) {}
