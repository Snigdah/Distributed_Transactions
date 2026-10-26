package com.leads.seatapoc.order.dto;

import java.math.BigDecimal;

// Sent to payment-service
public record ChargeRequest(String customerId, BigDecimal amount) {
}
