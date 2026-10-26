package com.leads.seatapoc.order.dto;

// What the customer sends
public record OrderRequest(String customerId, String productId, int quantity) {
}
