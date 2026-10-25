package com.leads.seatapoc.order.client;

import com.leads.seatapoc.order.dto.DeductStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Finds "inventory-service" in Eureka. No hardcoded URL.
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/inventory/deduct")
    void deductStock(@RequestBody DeductStockRequest request);
}
