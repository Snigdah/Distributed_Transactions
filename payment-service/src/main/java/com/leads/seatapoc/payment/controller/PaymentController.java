package com.leads.seatapoc.payment.controller;

import com.leads.seatapoc.payment.dto.ChargeRequest;
import com.leads.seatapoc.payment.entity.Wallet;
import com.leads.seatapoc.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Called by order-service
    @PostMapping("/charge")
    public Wallet charge(@RequestBody ChargeRequest request) {
        return paymentService.charge(request.orderId(), request.customerId(), request.amount());
    }

    // See wallet balance: GET /api/payments/wallet/C-001
    @GetMapping("/wallet/{customerId}")
    public Wallet getWallet(@PathVariable("customerId") String customerId) {
        return paymentService.getWallet(customerId);
    }

    // Turn fail mode on/off: POST /api/payments/fail-mode?enabled=true
    @PostMapping("/fail-mode")
    public String setFailMode(@RequestParam("enabled") boolean enabled) {
        paymentService.setFailMode(enabled);
        return "Fail mode is now " + (enabled ? "ON" : "OFF");
    }
}
