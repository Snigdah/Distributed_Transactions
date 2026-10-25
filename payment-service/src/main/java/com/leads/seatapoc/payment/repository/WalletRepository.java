package com.leads.seatapoc.payment.repository;

import com.leads.seatapoc.payment.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {

    // SELECT ... FOR UPDATE -> locks the row so two payments can't change it at the same time
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.customerId = :customerId")
    Optional<Wallet> findByIdForUpdate(@Param("customerId") String customerId);
}
