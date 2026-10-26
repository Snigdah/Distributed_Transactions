package com.leads.seatapoc.inventory.service;

import com.leads.seatapoc.inventory.entity.ProductStock;
import com.leads.seatapoc.inventory.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductStockRepository productStockRepository;

    @Transactional
    public ProductStock deduct(String productId, int quantity) {
        if (productStockRepository.deduct(productId, quantity) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for " + productId);
        }
        ProductStock stock = getStock(productId);
        log.info("XID {} -> stock of {} is now {}", RootContext.getXID(), productId, stock.getQuantity());
        return stock;
    }

    public ProductStock getStock(String productId) {
        return productStockRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));
    }
}
