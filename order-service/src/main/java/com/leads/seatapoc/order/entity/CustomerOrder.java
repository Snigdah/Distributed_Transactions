package com.leads.seatapoc.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// One order. Not called "Order" because ORDER is a reserved SQL word.
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Same key twice = same order (unique in DB)
    @Column(unique = true)
    private String idempotencyKey;

    private String customerId;

    private String productId;

    private int quantity;

    private BigDecimal amount;

    private String status;          // PENDING / CONFIRMED / CANCELLED

    @Column(length = 2000)
    private String failureReason;   // why it was cancelled

    private LocalDateTime createdAt = LocalDateTime.now();
}
