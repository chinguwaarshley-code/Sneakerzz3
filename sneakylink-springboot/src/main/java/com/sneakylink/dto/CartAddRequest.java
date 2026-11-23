package com.sneakylink.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CartAddRequest(
        @NotNull(message = "Shoe ID is required") Long shoeId,
        @NotBlank(message = "Size selection is required") String selectedSize,
        @NotNull @Min(1) Integer quantity
) {}
