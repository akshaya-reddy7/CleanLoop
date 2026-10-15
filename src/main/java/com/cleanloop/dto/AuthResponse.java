package com.cleanloop.dto;

import com.cleanloop.entity.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long userId,
        String email,
        String fullName,
        Role role,
        String serviceArea
) {
    public static AuthResponse of(
            String accessToken,
            String refreshToken,
            Long userId,
            String email,
            String fullName,
            Role role,
            String serviceArea) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                userId,
                email,
                fullName,
                role,
                serviceArea
        );
    }
}
