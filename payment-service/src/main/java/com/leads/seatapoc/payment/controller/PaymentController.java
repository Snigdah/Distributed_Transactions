package com.leads.seatapoc.payment.controller;

import com.leads.seatapoc.payment.dto.ChargeRequest;
import com.leads.seatapoc.payment.entity.Wallet;
import com.leads.seatapoc.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Called by order-service
    @PostMapping("/charge")
    public Wallet charge(@RequestBody ChargeRequest request) {
        return paymentService.charge(request.customerId(), request.amount());
    }

    // Check balance: GET /api/payments/wallet/C-001
    @GetMapping("/wallet/{customerId}")
    public Wallet getWallet(@PathVariable("customerId") String customerId) {
        return paymentService.getWallet(customerId);
    }
}
