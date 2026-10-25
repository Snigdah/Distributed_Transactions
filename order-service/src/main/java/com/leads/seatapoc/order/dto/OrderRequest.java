package com.leads.seatapoc.order.dto;

import java.math.BigDecimal;

// Body the customer sends
public record OrderRequest(String customerId, String productId, int quantity, BigDecimal amount) {
}
