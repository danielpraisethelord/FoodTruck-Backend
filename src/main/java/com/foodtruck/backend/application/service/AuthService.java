package com.foodtruck.backend.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foodtruck.backend.application.dto.AuthDtos.AuthRequest;
import com.foodtruck.backend.application.dto.AuthDtos.AuthResponse;
import com.foodtruck.backend.application.dto.AuthDtos.RefreshRequest;
import com.foodtruck.backend.application.dto.AuthDtos.RegisterRequest;
import com.foodtruck.backend.domain.model.Role;
import com.foodtruck.backend.domain.model.User;
import com.foodtruck.backend.domain.repository.UserRepository;
import com.foodtruck.backend.infrastructure.security.JwtService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final PasswordEncoder encoder;
    private final UserRepository repo;
    private final JwtService jwt;
    @Value("${security.jwt.expiration}")
    private long expiration;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        try {
            String username = req.username().trim().toLowerCase();
            String email = req.email().trim().toLowerCase();
            String name = req.name().trim();
            LocalDateTime registerDate = LocalDateTime.now();

            if (repo.existsByUsername(username) || repo.existsByEmail(email)) {
                throw new IllegalArgumentException("El usuario o email ya existe");
            }

            Set<Role> roles = (req.roles() == null || req.roles().isEmpty())
                    ? Set.of(Role.ROLE_USER)
                    : req.roles().stream()
                            .map(role -> Role.valueOf(role.toString()))
                            .collect(Collectors.toSet());

            var user = User.builder()
                    .username(username)
                    .email(email)
                    .password(encoder.encode(req.password()))
                    .roles(roles)
                    .name(name)
                    .registerDate(registerDate)
                    .build();

            repo.save(user);

            return buildTokens(user, roles);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("El usuario o email ya existe");
        }
    }

    public AuthResponse login(AuthRequest req) {
        String username = req.username().trim().toLowerCase();

        try {
            var user = repo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            var auth = new UsernamePasswordAuthenticationToken(username, req.password());
            authManager.authenticate(auth);

            return buildTokens(user, user.getRoles());

        } catch (UsernameNotFoundException | BadCredentialsException ex) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    public AuthResponse refresh(RefreshRequest req) {
        String username = jwt.extractUsername(req.refreshToken());
        var user = repo.findByUsername(username).orElseThrow();

        if (!jwt.isTokenValid(req.refreshToken(), username)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        return buildTokens(user, user.getRoles());
    }

    private AuthResponse buildTokens(User user, Set<Role> roles) {
        Map<String, Object> claims = Map.of("roles", roles);
        String access = jwt.generateAccessToken(user.getUsername(), claims);
        String refresh = jwt.generateRefreshToken(user.getUsername());

        return new AuthResponse(access, refresh, "Bearer", expiration);
    }
}
