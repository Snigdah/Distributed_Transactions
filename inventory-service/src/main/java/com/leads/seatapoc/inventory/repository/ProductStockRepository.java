package com.leads.seatapoc.inventory.repository;

import com.leads.seatapoc.inventory.entity.ProductStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductStockRepository extends JpaRepository<ProductStock, String> {

    // SELECT ... FOR UPDATE -> locks the row so two orders can't change it at the same time
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductStock p where p.productId = :productId")
    Optional<ProductStock> findByIdForUpdate(@Param("productId") String productId);
}
