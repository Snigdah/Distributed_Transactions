package com.leads.seatapoc.inventory.service;

import com.leads.seatapoc.inventory.entity.ProductStock;
import com.leads.seatapoc.inventory.entity.StockLedger;
import com.leads.seatapoc.inventory.repository.ProductStockRepository;
import com.leads.seatapoc.inventory.repository.StockLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductStockRepository productStockRepository;
    private final StockLedgerRepository stockLedgerRepository;

    @Transactional
    public ProductStock deductStock(Long orderId, String productId, int quantity) {
        // Find product and lock the row
        ProductStock stock = productStockRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Stock must not go below 0
        if (stock.getQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock");
        }

        // Reduce stock and save history
        stock.setQuantity(stock.getQuantity() - quantity);
        stockLedgerRepository.save(new StockLedger(orderId, productId, -quantity));

        log.info("Order {}: stock of {} is now {}", orderId, productId, stock.getQuantity());
        return stock;
    }

    public ProductStock getStock(String productId) {
        return productStockRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
