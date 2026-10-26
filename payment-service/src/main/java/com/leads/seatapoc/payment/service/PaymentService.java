package com.leads.seatapoc.payment.service;

import com.leads.seatapoc.payment.entity.Wallet;
import com.leads.seatapoc.payment.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
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

    @Transactional
    public Wallet charge(String customerId, BigDecimal amount) {
        if (walletRepository.charge(customerId, amount) == 0) {
            log.warn("XID {} -> not enough balance to pay {}", RootContext.getXID(), amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough balance to pay " + amount);
        }
        Wallet wallet = getWallet(customerId);
        log.info("XID {} -> wallet of {} is now {}", RootContext.getXID(), customerId, wallet.getBalance());
        return wallet;
    }

    public Wallet getWallet(String customerId) {
        return walletRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found: " + customerId));
    }
}
