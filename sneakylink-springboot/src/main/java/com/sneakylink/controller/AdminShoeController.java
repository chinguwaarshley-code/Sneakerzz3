package com.sneakylink.controller;

import com.sneakylink.dto.AdminStatsResponse;
import com.sneakylink.dto.ImageUpdateRequest;
import com.sneakylink.dto.ShoeRequest;
import com.sneakylink.dto.StockUpdateRequest;
import com.sneakylink.model.CustomerOrder;
import com.sneakylink.model.Shoe;
import com.sneakylink.service.AuthService;
import com.sneakylink.service.OrderService;
import com.sneakylink.service.ShoeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminShoeController {

    private final AuthService authService;
    private final ShoeService shoeService;
    private final OrderService orderService;

    public AdminShoeController(AuthService authService, ShoeService shoeService, OrderService orderService) {
        this.authService = authService;
        this.shoeService = shoeService;
        this.orderService = orderService;
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats(HttpSession session) {
        authService.requireManager(session);
        return orderService.getAdminStats();
    }

    @PostMapping("/shoes")
    @ResponseStatus(HttpStatus.CREATED)
    public Shoe createShoe(@Valid @RequestBody ShoeRequest request, HttpSession session) {
        authService.requireManager(session);
        return shoeService.createShoe(request);
    }

    @PutMapping("/shoes/{id}")
    public Shoe updateShoe(@PathVariable Long id, @Valid @RequestBody ShoeRequest request, HttpSession session) {
        authService.requireManager(session);
        return shoeService.updateShoe(id, request);
    }

    @PatchMapping("/shoes/{id}/stock")
    public Shoe updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request, HttpSession session) {
        authService.requireManager(session);
        return shoeService.updateStock(id, request.stockQuantity());
    }

    @PatchMapping("/shoes/{id}/image")
    public Shoe updateImage(@PathVariable Long id, @Valid @RequestBody ImageUpdateRequest request, HttpSession session) {
        authService.requireManager(session);
        return shoeService.updateImage(id, request.imageUrl());
    }

    @DeleteMapping("/shoes/{id}")
    public Map<String, String> deleteShoe(@PathVariable Long id, HttpSession session) {
        authService.requireManager(session);
        shoeService.deleteShoe(id);
        return Map.of("status", "deleted", "id", String.valueOf(id));
    }

    @GetMapping("/orders")
    public List<CustomerOrder> listAllOrders(HttpSession session) {
        authService.requireManager(session);
        return orderService.getAllOrders();
    }
}
