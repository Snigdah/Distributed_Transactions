package com.leads.seatapoc.order.client;

import com.leads.seatapoc.order.dto.DeductStockRequest;
import com.leads.seatapoc.order.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// "inventory-service" is found through Eureka, no hardcoded URL
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/inventory/deduct")
    ProductResponse deduct(@RequestBody DeductStockRequest request);
}
