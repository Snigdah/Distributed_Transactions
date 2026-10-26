package com.leads.seatapoc.inventory.controller;

import com.leads.seatapoc.inventory.dto.DeductStockRequest;
import com.leads.seatapoc.inventory.entity.ProductStock;
import com.leads.seatapoc.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Called by order-service
    @PostMapping("/deduct")
    public ProductStock deduct(@RequestBody DeductStockRequest request) {
        return inventoryService.deduct(request.productId(), request.quantity());
    }

    // Check stock: GET /api/inventory/P-100
    @GetMapping("/{productId}")
    public ProductStock getStock(@PathVariable("productId") String productId) {
        return inventoryService.getStock(productId);
    }
}
