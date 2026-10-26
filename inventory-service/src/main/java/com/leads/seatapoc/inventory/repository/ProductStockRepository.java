package com.leads.seatapoc.inventory.repository;

import com.leads.seatapoc.inventory.entity.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductStockRepository extends JpaRepository<ProductStock, String> {

    // Reduce stock only if enough is left. Returns 1 = done, 0 = not enough.
    @Modifying(clearAutomatically = true)
    @Query("update ProductStock p set p.quantity = p.quantity - :qty " +
           "where p.productId = :productId and p.quantity >= :qty")
    int deduct(@Param("productId") String productId, @Param("qty") int qty);
}
