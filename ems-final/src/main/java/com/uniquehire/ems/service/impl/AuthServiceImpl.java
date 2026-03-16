package com.uniquehire.ems.service.impl;

import com.uniquehire.ems.config.JwtUtil;
import com.uniquehire.ems.dto.LoginRequest;
import com.uniquehire.ems.dto.LoginResponse;
import com.uniquehire.ems.entity.User;
import com.uniquehire.ems.exception.BusinessException;
import com.uniquehire.ems.exception.ResourceNotFoundException;
import com.uniquehire.ems.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // ── LOGIN ────────────────────────────────────────────────

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt → username={}", request.getUsername());

        User user = userRepo.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException("Invalid username or password."));

        if (!user.getIsActive())
            throw new BusinessException("Your account is disabled. Please contact HR.");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BusinessException("Invalid username or password.");

        userRepo.updateLastLogin(user.getId(), ZonedDateTime.now());

        String token        = jwtUtil.generateToken(user.getUsername(),
                                  user.getRole().name(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        Long   employeeId = user.getEmployee() != null ? user.getEmployee().getId()        : null;
        String fullName   = user.getEmployee() != null ? user.getEmployee().getFullName()  : user.getUsername();

        log.info("Login SUCCESS → {} ({})", user.getUsername(), user.getRole());

        return LoginResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .role(user.getRole().name())
            .userId(user.getId())
            .employeeId(employeeId)
            .fullName(fullName)
            .expiresIn(expirationMs)
            .build();
    }

    // ── REFRESH TOKEN ────────────────────────────────────────

    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken))
            throw new BusinessException("Refresh token is invalid or expired. Please login again.");

        String username = jwtUtil.extractUsername(refreshToken);

        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!user.getIsActive())
            throw new BusinessException("Account is disabled.");

        String newToken = jwtUtil.generateToken(
            user.getUsername(), user.getRole().name(), user.getId());

        Long   employeeId = user.getEmployee() != null ? user.getEmployee().getId()       : null;
        String fullName   = user.getEmployee() != null ? user.getEmployee().getFullName() : user.getUsername();

        log.info("Token refreshed → username={}", username);

        return LoginResponse.builder()
            .token(newToken)
            .refreshToken(refreshToken)
            .role(user.getRole().name())
            .userId(user.getId())
            .employeeId(employeeId)
            .fullName(fullName)
            .expiresIn(expirationMs)
            .build();
    }

    // ── CHANGE PASSWORD ──────────────────────────────────────

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new BusinessException("Current password is incorrect.");

        if (newPassword.length() < 8)
            throw new BusinessException("New password must be at least 8 characters long.");

        if (passwordEncoder.matches(newPassword, user.getPassword()))
            throw new BusinessException("New password must be different from the current password.");

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        log.info("Password changed → userId={}", userId);
    }

    // ── CREATE USER ACCOUNT ──────────────────────────────────

    @Transactional
    public void createUserAccount(String username, String rawPassword, User.Role role) {
        if (userRepo.existsByUsername(username))
            throw new BusinessException("Username already taken: " + username);

        userRepo.save(User.builder()
            .username(username)
            .password(passwordEncoder.encode(rawPassword))
            .role(role)
            .isActive(true)
            .build());

        log.info("User account created → username={} role={}", username, role);
    }
}
