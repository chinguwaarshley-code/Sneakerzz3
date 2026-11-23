package com.sneakylink.service;

import com.sneakylink.dto.AuthRequest;
import com.sneakylink.dto.AuthResponse;
import com.sneakylink.exception.UnauthorizedException;
import com.sneakylink.model.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class AuthService {

    public static final String SESSION_USERNAME = "SL_USERNAME";
    public static final String SESSION_ROLE = "SL_ROLE";
    public static final String SESSION_CUSTOMER_KEY = "SL_CUSTOMER_KEY";

    private final String managerPassword;

    public AuthService(@Value("${sneakylink.manager.password:SneakyAdmin2026!}") String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public AuthResponse login(AuthRequest request, HttpSession session) {
        String cleanUsername = (request.username() == null || request.username().isBlank())
                ? (request.role() == Role.MANAGER ? "Inventory Manager" : "Guest-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                : request.username().trim();

        if (request.role() == Role.MANAGER) {
            if (request.password() == null || !constantTimeEquals(request.password(), managerPassword)) {
                throw new UnauthorizedException("Invalid manager password. Access to the SneakyLink Admin Portal is restricted.");
            }
        }

        String customerKey = normalizeCustomerKey(cleanUsername, request.role());
        session.setAttribute(SESSION_USERNAME, cleanUsername);
        session.setAttribute(SESSION_ROLE, request.role());
        session.setAttribute(SESSION_CUSTOMER_KEY, customerKey);

        String message = request.role() == Role.MANAGER
                ? "Manager authenticated. Welcome to the SneakyLink Inventory Backend."
                : "Welcome to SneakyLink, " + cleanUsername + ".";

        return new AuthResponse(true, cleanUsername, customerKey, request.role(), message);
    }

    public AuthResponse currentSession(HttpSession session) {
        Object roleObj = session.getAttribute(SESSION_ROLE);
        if (roleObj instanceof Role role) {
            String username = (String) session.getAttribute(SESSION_USERNAME);
            String customerKey = (String) session.getAttribute(SESSION_CUSTOMER_KEY);
            return new AuthResponse(true, username, customerKey, role, "Active session");
        }
        return new AuthResponse(false, null, null, null, "Not authenticated");
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public void requireManager(HttpSession session) {
        Object roleObj = session.getAttribute(SESSION_ROLE);
        if (!(roleObj instanceof Role role) || role != Role.MANAGER) {
            throw new UnauthorizedException("Manager role required to access this endpoint.");
        }
    }

    public String resolveCustomerKey(HttpSession session) {
        Object keyObj = session.getAttribute(SESSION_CUSTOMER_KEY);
        if (keyObj instanceof String key && !key.isBlank()) {
            return key;
        }
        String fallback = "guest-" + session.getId().substring(0, Math.min(12, session.getId().length()));
        session.setAttribute(SESSION_CUSTOMER_KEY, fallback);
        return fallback;
    }

    private String normalizeCustomerKey(String username, Role role) {
        String slug = username.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isEmpty()) {
            slug = UUID.randomUUID().toString().substring(0, 8);
        }
        return role.name().toLowerCase() + ":" + slug;
    }

    private boolean constantTimeEquals(String provided, String expected) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }
}
