package com.nttdata.account_service.web;

import com.nttdata.account_service.service.TransferService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/own")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> transferOwn(@RequestBody TransferRequest r) {
        return transferService.transferSameCustomer(r.getFromAccountId(), r.getToAccountId(), r.getAmount());
    }

    @PostMapping("/third-party")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> transferThirdParty(@RequestBody TransferRequest r) {
        return transferService.transferThirdParty(r.getFromAccountId(), r.getToAccountId(), r.getAmount());
    }

    @Data
    public static class TransferRequest {
        private String fromAccountId;
        private String toAccountId;
        private BigDecimal amount;
    }
}