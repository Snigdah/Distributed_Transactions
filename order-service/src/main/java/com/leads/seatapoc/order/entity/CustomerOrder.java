package com.leads.seatapoc.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// Not named "Order" because ORDER is a reserved SQL word
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)   // same in PostgreSQL and Oracle
    private Long id;

    private String customerId;

    private String productId;

    private int quantity;

    private BigDecimal amount;

    private String status;   // CONFIRMED
}
