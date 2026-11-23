package com.sneakylink.dto;

import java.math.BigDecimal;

public record AdminStatsResponse(
        long totalModels,
        long totalUnitsInStock,
        long lowStockModels,
        long outOfStockModels,
        long totalOrders,
        BigDecimal totalRevenue
) {}
