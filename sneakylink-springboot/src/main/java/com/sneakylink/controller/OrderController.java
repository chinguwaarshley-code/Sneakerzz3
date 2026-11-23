package com.sneakylink.controller;

import com.sneakylink.dto.CartSummaryResponse;
import com.sneakylink.dto.CheckoutRequest;
import com.sneakylink.dto.FulfillmentOptionsResponse;
import com.sneakylink.model.CustomerOrder;
import com.sneakylink.service.AuthService;
import com.sneakylink.service.CartService;
import com.sneakylink.service.FulfillmentService;
import com.sneakylink.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final FulfillmentService fulfillmentService;
    private final AuthService authService;

    public OrderController(OrderService orderService,
                           CartService cartService,
                           FulfillmentService fulfillmentService,
                           AuthService authService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.fulfillmentService = fulfillmentService;
        this.authService = authService;
    }

    @GetMapping("/fulfillment-options")
    public FulfillmentOptionsResponse fulfillmentOptions(HttpSession session) {
        CartSummaryResponse cart = cartService.getCart(authService.resolveCustomerKey(session));
        return fulfillmentService.calculateOptions(cart.subtotal());
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder checkout(@Valid @RequestBody CheckoutRequest request, HttpSession session) {
        return orderService.checkout(authService.resolveCustomerKey(session), request);
    }

    @GetMapping
    public List<CustomerOrder> myOrders(HttpSession session) {
        return orderService.getOrdersForCustomer(authService.resolveCustomerKey(session));
    }
}
