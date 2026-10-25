package com.leads.seatapoc.order.dto;

// Body we send to inventory-service
public record DeductStockRequest(Long orderId, String productId, int quantity) {
}
