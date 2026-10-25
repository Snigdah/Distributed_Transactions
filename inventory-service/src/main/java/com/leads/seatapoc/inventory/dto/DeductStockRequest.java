package com.leads.seatapoc.inventory.dto;

// Body sent by order-service
public record DeductStockRequest(Long orderId, String productId, int quantity) {
}
