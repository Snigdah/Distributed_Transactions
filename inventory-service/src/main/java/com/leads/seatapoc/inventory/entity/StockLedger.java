package com.leads.seatapoc.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// History of stock changes. Example: order 1 took 1 item -> quantityChange = -1
@Entity
@Table(name = "stock_ledger")
@Getter
@NoArgsConstructor
public class StockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String productId;

    private int quantityChange;

    private LocalDateTime createdAt = LocalDateTime.now();

    public StockLedger(Long orderId, String productId, int quantityChange) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantityChange = quantityChange;
    }
}
