package com.cleanloop.service;

import com.cleanloop.dto.ChangePasswordRequest;
import com.cleanloop.dto.UpdateProfileRequest;
import com.cleanloop.dto.UserProfileResponse;
import com.cleanloop.entity.User;
import com.cleanloop.exception.BadRequestException;
import com.cleanloop.exception.ResourceNotFoundException;
import com.cleanloop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = getUserById(userId);
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        user.setFullName(request.fullName().trim());
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }
        if (request.serviceArea() != null) {
            user.setServiceArea(request.serviceArea().trim());
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user ID: {}", userId);
        return UserProfileResponse.fromEntity(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password cannot be the same as the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public com.cleanloop.dto.PagedResponse<UserProfileResponse> getAllUsers(com.cleanloop.entity.Role role, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<User> page = (role != null)
                ? userRepository.findByRole(role, pageable)
                : userRepository.findAll(pageable);
        return com.cleanloop.dto.PagedResponse.from(page.map(UserProfileResponse::fromEntity));
    }

    @Transactional
    public UserProfileResponse updateUserRole(Long userId, com.cleanloop.entity.Role newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        User saved = userRepository.save(user);
        log.info("User ID {} role updated to {}", userId, newRole);
        return UserProfileResponse.fromEntity(saved);
    }

    @Transactional
    public UserProfileResponse updateUserStatus(Long userId, boolean active) {
        User user = getUserById(userId);
        user.setActive(active);
        User saved = userRepository.save(user);
        log.info("User ID {} active status updated to {}", userId, active);
        return UserProfileResponse.fromEntity(saved);
    }
}
