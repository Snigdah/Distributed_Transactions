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
    // If any step throws, Seata undoes the order, the stock and the payment.
    @GlobalTransactional(name = "place-order", rollbackFor = Exception.class)
    public CustomerOrder placeOrder(OrderRequest request) {
        log.info("Global transaction started, XID {}", RootContext.getXID());

        // 1. Save order (order_db)
        CustomerOrder order = new CustomerOrder();
        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setStatus("PENDING");
        order = orderRepository.save(order);

        // 2. Reduce stock (inventory_db) and get the price
        ProductResponse product = inventoryClient.deduct(
                new DeductStockRequest(request.productId(), request.quantity()));
        BigDecimal amount = product.price().multiply(BigDecimal.valueOf(request.quantity()));

        // 3. Take money (payment_db). Fails if the wallet is too small.
        paymentClient.charge(new ChargeRequest(request.customerId(), amount));

        // 4. All good
        order.setAmount(amount);
        order.setStatus("CONFIRMED");
        log.info("Order {} CONFIRMED, XID {} will commit", order.getId(), RootContext.getXID());
        return orderRepository.save(order);
    }

    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }
}
