package com.sneakylink.controller;

import com.sneakylink.dto.CartAddRequest;
import com.sneakylink.dto.CartSummaryResponse;
import com.sneakylink.dto.CartUpdateRequest;
import com.sneakylink.service.AuthService;
import com.sneakylink.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    @GetMapping
    public CartSummaryResponse getCart(HttpSession session) {
        return cartService.getCart(authService.resolveCustomerKey(session));
    }

    @PostMapping("/items")
    public CartSummaryResponse addItem(@Valid @RequestBody CartAddRequest request, HttpSession session) {
        return cartService.addItem(authService.resolveCustomerKey(session), request);
    }

    @PutMapping("/items/{id}")
    public CartSummaryResponse updateItem(@PathVariable Long id, @Valid @RequestBody CartUpdateRequest request, HttpSession session) {
        return cartService.updateItemQuantity(authService.resolveCustomerKey(session), id, request.quantity());
    }

    @DeleteMapping("/items/{id}")
    public CartSummaryResponse removeItem(@PathVariable Long id, HttpSession session) {
        return cartService.removeItem(authService.resolveCustomerKey(session), id);
    }

    @DeleteMapping
    public CartSummaryResponse clearCart(HttpSession session) {
        return cartService.clearCart(authService.resolveCustomerKey(session));
    }
}
