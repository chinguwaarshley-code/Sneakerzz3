package com.sneakylink.dto;

import com.sneakylink.model.Role;

public record AuthResponse(
        boolean authenticated,
        String username,
        String customerKey,
        Role role,
        String message
) {}
