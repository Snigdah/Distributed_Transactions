package com.leads.seatapoc.order.controller;

import com.leads.seatapoc.order.dto.OrderRequest;
import com.leads.seatapoc.order.entity.CustomerOrder;
import com.leads.seatapoc.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public CustomerOrder placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping
    public List<CustomerOrder> getAllOrders() {
        return orderService.getAllOrders();
    }
}
