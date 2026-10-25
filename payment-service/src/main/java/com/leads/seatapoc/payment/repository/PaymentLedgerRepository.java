package com.leads.seatapoc.payment.repository;

import com.leads.seatapoc.payment.entity.PaymentLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentLedgerRepository extends JpaRepository<PaymentLedger, Long> {
}
