package com.kanishka.demo.auth;

import com.kanishka.demo.auth.dto.RegisterRequest;
import com.kanishka.demo.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest req) {

        // ── Password match ──
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        // ── Password strength ──
        if (!isStrongPassword(req.getPassword())) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and contain "
                            + "an uppercase letter, a lowercase letter, a number, "
                            + "and a special character.");
        }

        // ── Email uniqueness ──
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "This email address is already registered.");
        }

        // ── Name check ──
        if (req.getFullName() == null || req.getFullName().trim().length() < 3) {
            throw new IllegalArgumentException(
                    "Full name must be at least 3 characters.");
        }

        // ── Phone format (optional but validate if present) ──
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            String phone = req.getPhone().replaceAll("[\\s\\-()]", "");
            if (!phone.matches("^[+]?[0-9]{9,15}$")) {
                throw new IllegalArgumentException(
                        "Please enter a valid phone number.");
            }
        }

        User user = User.builder()
                .fullName(req.getFullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone() != null ? req.getPhone().trim() : null)
                .address(req.getAddress() != null ? req.getAddress().trim() : null)
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        userRepository.save(user);
    }

    // ── Password must be 8+ chars, upper, lower, digit, special ──
    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars()
                .anyMatch(c -> "!@#$%^&*()_+-=[]{}|;':\",./<>?".indexOf(c) >= 0);
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}