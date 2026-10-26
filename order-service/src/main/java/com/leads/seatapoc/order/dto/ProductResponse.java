package com.leads.seatapoc.order.dto;

import java.math.BigDecimal;

// Returned by inventory-service (we need the price)
public record ProductResponse(String productId, BigDecimal price, int quantity) {
}
