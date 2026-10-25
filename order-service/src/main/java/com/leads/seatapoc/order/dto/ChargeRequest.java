package com.leads.seatapoc.order.dto;

import java.math.BigDecimal;

// Body we send to payment-service
public record ChargeRequest(Long orderId, String customerId, BigDecimal amount) {
}
