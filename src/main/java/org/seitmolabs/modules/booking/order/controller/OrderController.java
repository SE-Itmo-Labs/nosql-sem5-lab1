package org.seitmolabs.modules.booking.order.controller;

import java.util.List;

import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.booking.order.dto.CreateOrderRequest;
import org.seitmolabs.modules.booking.order.dto.OrderResponse;
import org.seitmolabs.modules.booking.order.service.OrderService;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> confirm(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.confirm(request, user));
    }

    @GetMapping
    public List<OrderResponse> getOwnOrders(
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User user
    ) {
        return orderService.getOwnOrders(user);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOwnOrder(
            @PathVariable Long orderId,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User user
    ) {
        return orderService.getOwnOrder(orderId, user);
    }
}
