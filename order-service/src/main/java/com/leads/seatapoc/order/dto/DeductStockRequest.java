package com.leads.seatapoc.order.dto;

// Sent to inventory-service
public record DeductStockRequest(String productId, int quantity) {
}
