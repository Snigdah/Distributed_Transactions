package com.leads.seatapoc.order.service;

import com.leads.seatapoc.order.client.InventoryClient;
import com.leads.seatapoc.order.client.PaymentClient;
import com.leads.seatapoc.order.dto.ChargeRequest;
import com.leads.seatapoc.order.dto.DeductStockRequest;
import com.leads.seatapoc.order.dto.OrderRequest;
import com.leads.seatapoc.order.entity.CustomerOrder;
import com.leads.seatapoc.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    // Flow: save order -> take stock -> take money -> CONFIRMED
    // No Seata yet: if payment fails, the stock is NOT given back.
    public CustomerOrder placeOrder(String idempotencyKey, OrderRequest request) {

        // 1. Same key already used? Return the old order (no double charge)
        Optional<CustomerOrder> oldOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (oldOrder.isPresent()) {
            log.info("Key {} already used, returning order {}", idempotencyKey, oldOrder.get().getId());
            return oldOrder.get();
        }

        // 2. Save new order as PENDING
        CustomerOrder order = new CustomerOrder();
        order.setIdempotencyKey(idempotencyKey);
        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setAmount(request.amount());
        order.setStatus("PENDING");
        order = orderRepository.save(order);

        try {
            // 3. Take stock (inventory-service)
            inventoryClient.deductStock(new DeductStockRequest(order.getId(), request.productId(), request.quantity()));

            // 4. Take money (payment-service)
            paymentClient.charge(new ChargeRequest(order.getId(), request.customerId(), request.amount()));

            // 5. Both done
            order.setStatus("CONFIRMED");
            log.info("Order {} CONFIRMED", order.getId());

        } catch (Exception e) {
            // Something failed -> cancel
            order.setStatus("CANCELLED");
            order.setFailureReason(e.getMessage());
            log.warn("Order {} CANCELLED: {}", order.getId(), e.getMessage());
        }

        return orderRepository.save(order);
    }

    public CustomerOrder getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
