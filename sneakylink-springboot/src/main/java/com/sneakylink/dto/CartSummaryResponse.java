package com.sneakylink.dto;

import com.sneakylink.model.CartItem;
import java.math.BigDecimal;
import java.util.List;

public record CartSummaryResponse(
        String customerKey,
        List<CartItem> items,
        int totalQuantity,
        BigDecimal subtotal,
        BigDecimal standardShippingFee,
        BigDecimal expressShippingFee
) {}
