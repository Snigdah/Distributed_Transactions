package com.leads.seatapoc.inventory.controller;

import com.leads.seatapoc.inventory.dto.DeductStockRequest;
import com.leads.seatapoc.inventory.entity.ProductStock;
import com.leads.seatapoc.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Called by order-service
    @PostMapping("/deduct")
    public ProductStock deductStock(@RequestBody DeductStockRequest request) {
        return inventoryService.deductStock(request.orderId(), request.productId(), request.quantity());
    }

    // See current stock: GET /api/inventory/P-100
    @GetMapping("/{productId}")
    public ProductStock getStock(@PathVariable("productId") String productId) {
        return inventoryService.getStock(productId);
    }
}
