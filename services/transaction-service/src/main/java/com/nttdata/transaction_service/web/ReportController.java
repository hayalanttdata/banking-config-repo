package com.nttdata.transaction_service.web;

import com.nttdata.transaction_service.application.ReportAppService;
import com.nttdata.transaction_service.dto.CommissionReportDto;
import com.nttdata.transaction_service.dto.DailyBalanceReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportAppService reportService;

    @GetMapping("/customers/{customerId}/daily-balance")
    public Flux<DailyBalanceReportDto> getDailyBalanceReport(@PathVariable String customerId) {
        return reportService.generateDailyBalanceReport(customerId);
    }

    @GetMapping("/commissions")
    public Flux<CommissionReportDto> getCommissionsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.generateCommissionReport(from, to);
    }

    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("report-service is UP");
    }
}