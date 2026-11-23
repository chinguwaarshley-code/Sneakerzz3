package com.sneakylink.dto;

import com.sneakylink.model.Role;
import jakarta.validation.constraints.NotNull;

public record AuthRequest(
        String username,
        @NotNull(message = "Role is required") Role role,
        String password
) {}
