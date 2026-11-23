package com.sneakylink.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ShoeRequest(
        @NotBlank(message = "SKU is required") String sku,
        @NotBlank(message = "Model name is required") String name,
        @NotBlank(message = "Brand is required") String brand,
        @NotBlank(message = "Category is required") String category,
        @NotBlank(message = "Colorway is required") String colorway,
        @NotBlank(message = "Primary color is required") String primaryColor,
        @NotBlank(message = "Size range is required") String sizeRange,
        @NotBlank(message = "Available sizes are required") String availableSizes,
        @NotBlank(message = "Material is required") String material,
        @NotNull(message = "Price is required") @DecimalMin(value = "1.00") BigDecimal price,
        @NotNull(message = "Stock quantity is required") @Min(0) Integer stockQuantity,
        @NotBlank(message = "Image URL is required") String imageUrl,
        String description,
        boolean featured
) {}
