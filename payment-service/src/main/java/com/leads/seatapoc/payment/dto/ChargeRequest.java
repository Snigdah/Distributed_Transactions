package com.leads.seatapoc.payment.dto;

import java.math.BigDecimal;

public record ChargeRequest(String customerId, BigDecimal amount) {
}
