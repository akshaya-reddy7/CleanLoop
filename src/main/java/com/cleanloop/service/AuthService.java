package com.cleanloop.service;

import com.cleanloop.dto.AuthResponse;
import com.cleanloop.dto.LoginRequest;
import com.cleanloop.dto.RefreshTokenRequest;
import com.cleanloop.dto.RegisterRequest;
import com.cleanloop.entity.Role;
import com.cleanloop.entity.User;
import com.cleanloop.exception.BadRequestException;
import com.cleanloop.exception.ConflictException;
import com.cleanloop.exception.UnauthorizedException;
import com.cleanloop.repository.UserRepository;
import com.cleanloop.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered: " + normalizedEmail);
        }

        // Public registration explicitly assigns CITIZEN role to prevent privilege escalation
        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.phoneNumber() != null ? request.phoneNumber().trim() : null,
                Role.CITIZEN,
                null
        );

        User savedUser = userRepository.save(user);
        log.info("New citizen registered successfully with ID: {}", savedUser.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getServiceArea()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getServiceArea()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found for provided token"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        // Can optionally rotate refresh token or return existing one
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getServiceArea()
        );
    }
}
