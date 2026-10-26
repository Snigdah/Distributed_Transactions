package com.leads.seatapoc.order.service;

import com.leads.seatapoc.order.client.InventoryClient;
import com.leads.seatapoc.order.client.PaymentClient;
import com.leads.seatapoc.order.dto.ChargeRequest;
import com.leads.seatapoc.order.dto.DeductStockRequest;
import com.leads.seatapoc.order.dto.OrderRequest;
import com.leads.seatapoc.order.dto.ProductResponse;
import com.leads.seatapoc.order.entity.CustomerOrder;
import com.leads.seatapoc.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    // One global transaction over 3 databases.
    // If any step throws, Seata tells every DB to roll back (order, stock, payment).
    @GlobalTransactional(name = "place-order", rollbackFor = Exception.class)
    public CustomerOrder placeOrder(OrderRequest request) {
        log.info("Global transaction started, XID {}", RootContext.getXID());

        // 1. Reduce stock (inventory_db) and get the price
        ProductResponse product = inventoryClient.deduct(
                new DeductStockRequest(request.productId(), request.quantity()));
        BigDecimal amount = product.price().multiply(BigDecimal.valueOf(request.quantity()));

        // 2. Take money (payment_db). Fails if the wallet is too small.
        paymentClient.charge(new ChargeRequest(request.customerId(), amount));

        // 3. Save the order once (order_db).
        // XA: one save only. Each save is its own XA branch, and a second branch
        // cannot see the first one's row until the global commit.
        CustomerOrder order = new CustomerOrder();
        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setAmount(amount);
        order.setStatus("CONFIRMED");
        order = orderRepository.save(order);
        log.info("Order {} CONFIRMED, XID {} will commit", order.getId(), RootContext.getXID());
        return order;
    }

    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }
}
