package com.sneakylink.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageUpdateRequest(
        @NotBlank(message = "Image URL is required")
        String imageUrl
) {}
