package com.leads.seatapoc.order.controller;

import com.leads.seatapoc.order.dto.OrderRequest;
import com.leads.seatapoc.order.entity.CustomerOrder;
import com.leads.seatapoc.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Place order. Send a new Idempotency-Key for every new order.
    @PostMapping
    public CustomerOrder placeOrder(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                    @RequestBody OrderRequest request) {
        return orderService.placeOrder(idempotencyKey, request);
    }

    // See an order: GET /api/orders/1
    @GetMapping("/{id}")
    public CustomerOrder getOrder(@PathVariable("id") Long id) {
        return orderService.getOrder(id);
    }
}
