package com.leads.seatapoc.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// History of wallet changes. Example: order 1 paid 1000 Tk -> amountChange = -1000
@Entity
@Table(name = "payment_ledger")
@Getter
@NoArgsConstructor
public class PaymentLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private String customerId;

    private BigDecimal amountChange;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PaymentLedger(Long orderId, String customerId, BigDecimal amountChange) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amountChange = amountChange;
    }
}
