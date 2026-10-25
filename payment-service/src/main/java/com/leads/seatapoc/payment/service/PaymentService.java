package com.leads.seatapoc.payment.service;

import com.leads.seatapoc.payment.entity.PaymentLedger;
import com.leads.seatapoc.payment.entity.Wallet;
import com.leads.seatapoc.payment.repository.PaymentLedgerRepository;
import com.leads.seatapoc.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WalletRepository walletRepository;
    private final PaymentLedgerRepository paymentLedgerRepository;

    // Test switch: true = every payment fails (to test rollback)
    @Setter
    private volatile boolean failMode = false;

    @Transactional
    public Wallet charge(Long orderId, String customerId, BigDecimal amount) {
        // Fake failure for testing
        if (failMode) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment failed (fail mode ON)");
        }

        // Find wallet and lock the row
        Wallet wallet = walletRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        // Balance must not go below 0
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough balance");
        }

        // Take money and save history
        wallet.setBalance(wallet.getBalance().subtract(amount));
        paymentLedgerRepository.save(new PaymentLedger(orderId, customerId, amount.negate()));

        log.info("Order {}: wallet of {} is now {}", orderId, customerId, wallet.getBalance());
        return wallet;
    }

    public Wallet getWallet(String customerId) {
        return walletRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
    }
}
