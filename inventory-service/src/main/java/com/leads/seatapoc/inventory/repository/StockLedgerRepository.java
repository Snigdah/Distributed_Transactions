package com.leads.seatapoc.inventory.repository;

import com.leads.seatapoc.inventory.entity.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
}
