package com.sneakylink.repository;

import com.sneakylink.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCustomerKeyOrderByAddedAtAsc(String customerKey);

    Optional<CartItem> findByCustomerKeyAndShoeIdAndSelectedSize(String customerKey, Long shoeId, String selectedSize);

    void deleteByCustomerKey(String customerKey);

    void deleteByShoeId(Long shoeId);
}
