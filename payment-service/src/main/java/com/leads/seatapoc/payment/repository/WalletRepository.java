package com.leads.seatapoc.payment.repository;

import com.leads.seatapoc.payment.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface WalletRepository extends JpaRepository<Wallet, String> {

    // Take money only if enough is left. Returns 1 = done, 0 = not enough.
    @Modifying(clearAutomatically = true)
    @Query("update Wallet w set w.balance = w.balance - :amount " +
           "where w.customerId = :customerId and w.balance >= :amount")
    int charge(@Param("customerId") String customerId, @Param("amount") BigDecimal amount);
}
