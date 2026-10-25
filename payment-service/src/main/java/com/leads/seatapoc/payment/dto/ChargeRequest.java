package com.leads.seatapoc.payment.dto;

import java.math.BigDecimal;

// Body sent by order-service
public record ChargeRequest(Long orderId, String customerId, BigDecimal amount) {
}
