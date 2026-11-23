package com.sneakylink.dto;

import com.sneakylink.model.FulfillmentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotBlank(message = "Full name is required") String customerName,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String customerEmail,
        @NotBlank(message = "Phone number is required") String customerPhone,
        @NotNull(message = "Fulfillment type is required") FulfillmentType fulfillmentType,
        String shippingAddress,
        String shippingCity,
        String shippingPostalCode,
        String selectedPickupWindow,
        @NotBlank(message = "Payment method is required") String paymentMethod,
        String cardNumber
) {}
