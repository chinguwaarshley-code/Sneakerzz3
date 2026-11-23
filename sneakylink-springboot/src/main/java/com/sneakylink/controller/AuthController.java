package com.sneakylink.controller;

import com.sneakylink.dto.AuthRequest;
import com.sneakylink.dto.AuthResponse;
import com.sneakylink.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        return authService.login(request, session);
    }

    @GetMapping("/me")
    public AuthResponse me(HttpSession session) {
        return authService.currentSession(session);
    }

    @PostMapping("/logout")
    public AuthResponse logout(HttpSession session) {
        authService.logout(session);
        return new AuthResponse(false, null, null, null, "Signed out successfully.");
    }
}
