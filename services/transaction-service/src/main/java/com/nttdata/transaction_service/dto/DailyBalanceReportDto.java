package com.nttdata.transaction_service.dto;

import java.math.BigDecimal;

public record DailyBalanceReportDto(
        String productId,
        String productType,
        BigDecimal averageDailyBalance
) {}