package com.cleanloop.dto;

import com.cleanloop.entity.Role;
import com.cleanloop.entity.User;
import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        String phoneNumber,
        Role role,
        String serviceArea,
        boolean isActive,
        Instant createdAt
) {
    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getServiceArea(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
