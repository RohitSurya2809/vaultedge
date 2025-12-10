package com.rohitsurya2809.vaultedge.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class TransactionSummaryResponse {
    private UUID accountId;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal netFlow;
    private long count;
    private Map<String, Long> byType;

    public TransactionSummaryResponse() {}

    public TransactionSummaryResponse(UUID accountId,
                                      BigDecimal totalDeposits,
                                      BigDecimal totalWithdrawals,
                                      BigDecimal netFlow,
                                      long count,
                                      Map<String, Long> byType) {
        this.accountId = accountId;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.netFlow = netFlow;
        this.count = count;
        this.byType = byType;
    }

    // getters & setters
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public BigDecimal getTotalDeposits() { return totalDeposits; }
    public void setTotalDeposits(BigDecimal totalDeposits) { this.totalDeposits = totalDeposits; }

    public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
    public void setTotalWithdrawals(BigDecimal totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }

    public BigDecimal getNetFlow() { return netFlow; }
    public void setNetFlow(BigDecimal netFlow) { this.netFlow = netFlow; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public Map<String, Long> getByType() { return byType; }
    public void setByType(Map<String, Long> byType) { this.byType = byType; }
}
