package com.sneakylink.service;

import com.sneakylink.dto.CartAddRequest;
import com.sneakylink.dto.CartSummaryResponse;
import com.sneakylink.exception.InsufficientStockException;
import com.sneakylink.exception.ResourceNotFoundException;
import com.sneakylink.model.CartItem;
import com.sneakylink.model.Shoe;
import com.sneakylink.repository.CartItemRepository;
import com.sneakylink.repository.ShoeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ShoeRepository shoeRepository;

    public CartService(CartItemRepository cartItemRepository, ShoeRepository shoeRepository) {
        this.cartItemRepository = cartItemRepository;
        this.shoeRepository = shoeRepository;
    }

    public CartSummaryResponse getCart(String customerKey) {
        List<CartItem> items = cartItemRepository.findByCustomerKeyOrderByAddedAtAsc(customerKey);
        int totalQty = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal subtotal = items.stream()
                .map(i -> i.getShoe().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal stdFee = subtotal.compareTo(FulfillmentService.FREE_SHIPPING_THRESHOLD) >= 0 || subtotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : FulfillmentService.STANDARD_SHIPPING_FEE;

        return new CartSummaryResponse(
                customerKey,
                items,
                totalQty,
                subtotal,
                stdFee,
                FulfillmentService.EXPRESS_SHIPPING_FEE
        );
    }

    @Transactional
    public CartSummaryResponse addItem(String customerKey, CartAddRequest request) {
        Shoe shoe = shoeRepository.findById(request.shoeId())
                .orElseThrow(() -> new ResourceNotFoundException("Shoe model not found with ID: " + request.shoeId()));

        if (shoe.getStockQuantity() <= 0) {
            throw new InsufficientStockException(shoe.getName() + " is currently out of stock.");
        }

        CartItem item = cartItemRepository
                .findByCustomerKeyAndShoeIdAndSelectedSize(customerKey, shoe.getId(), request.selectedSize().trim())
                .orElseGet(() -> new CartItem(customerKey, shoe, request.selectedSize().trim(), 0));

        int updatedQty = item.getQuantity() + request.quantity();
        if (updatedQty > shoe.getStockQuantity()) {
            throw new InsufficientStockException("Only " + shoe.getStockQuantity() + " pair(s) of " + shoe.getName() + " available in stock.");
        }

        item.setQuantity(updatedQty);
        cartItemRepository.save(item);
        return getCart(customerKey);
    }

    @Transactional
    public CartSummaryResponse updateItemQuantity(String customerKey, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .filter(ci -> ci.getCustomerKey().equals(customerKey))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));

        if (quantity > item.getShoe().getStockQuantity()) {
            throw new InsufficientStockException("Only " + item.getShoe().getStockQuantity() + " pair(s) available in stock.");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCart(customerKey);
    }

    @Transactional
    public CartSummaryResponse removeItem(String customerKey, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .filter(ci -> ci.getCustomerKey().equals(customerKey))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));
        cartItemRepository.delete(item);
        return getCart(customerKey);
    }

    @Transactional
    public CartSummaryResponse clearCart(String customerKey) {
        cartItemRepository.deleteByCustomerKey(customerKey);
        return getCart(customerKey);
    }
}
