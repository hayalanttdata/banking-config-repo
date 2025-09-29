package com.nttdata.transaction_service.dto;

import java.math.BigDecimal;

public record CommissionReportDto(
        String productId,
        String productType,
        BigDecimal totalCommission
) {}