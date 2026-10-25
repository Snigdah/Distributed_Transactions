package com.leads.seatapoc.order.client;

import com.leads.seatapoc.order.dto.ChargeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Finds "payment-service" in Eureka. No hardcoded URL.
@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payments/charge")
    void charge(@RequestBody ChargeRequest request);
}
