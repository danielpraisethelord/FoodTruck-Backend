package com.foodtruck.backend.application.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

import com.foodtruck.backend.domain.model.Role;

public class AuthDtos {

        public record RegisterRequest(
                        @NotBlank String username,
                        @Email @NotBlank String email,
                        @Size(min = 8) String password,
                        Set<Role> roles) {
        }

        public record AuthRequest(
                        @NotBlank String username,
                        @NotBlank String password) {
        }

        public record RefreshRequest(
                        @NotBlank String refreshToken) {
        }

        public record AuthResponse(
                        String accessToken,
                        String refreshToken,
                        String tokenType,
                        long expiresInMs) {
        }
}