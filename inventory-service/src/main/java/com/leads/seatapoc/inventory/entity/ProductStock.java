package com.leads.seatapoc.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// How many items of a product we have
@Entity
@Table(name = "product_stock")
@Getter
@Setter
@NoArgsConstructor
public class ProductStock {

    @Id
    private String productId;

    private int quantity;
}
