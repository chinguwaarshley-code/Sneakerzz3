package com.sneakylink.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FulfillmentOptionsResponse(
        LocalDate standardDeliveryStart,
        LocalDate standardDeliveryEnd,
        String standardDeliveryLabel,
        BigDecimal standardShippingFee,
        LocalDate expressDeliveryDate,
        String expressDeliveryLabel,
        BigDecimal expressShippingFee,
        String storeName,
        String storeAddress,
        List<String> pickupWindows
) {}
