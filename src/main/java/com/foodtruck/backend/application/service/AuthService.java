package com.foodtruck.backend.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
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

import java.util.*;

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
        // TODO: Convertir username y email a lowercase antes de guardar
        // TODO: Manejar excepciones específicas (DataIntegrityViolationException para
        // duplicados)
        if (repo.existsByUsername(req.username()) || repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("El usuario o email ya existe");
        }

        Set<Role> roles = (req.roles() == null || req.roles().isEmpty())
                ? Set.of(Role.ROLE_USER)
                : req.roles().stream().map(Role::valueOf).collect(java.util.stream.Collectors.toSet());

        var user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(encoder.encode(req.password()))
                .roles(roles)
                .build();

        repo.save(user);

        return buildTokens(user, roles);
    }

    public AuthResponse login(AuthRequest req) {
        // TODO: Normalizar username a lowercase para búsqueda
        // TODO: Manejar UsernameNotFoundException y BadCredentialsException
        System.out.println("Attempting to authenticate user: " + req.username());
        var user = repo.findByUsername(req.username()).orElseThrow(() -> {
            System.out.println("User not found: " + req.username());
            return new RuntimeException("Usuario no encontrado");
        });

        System.out.println("User found: " + user.getUsername());
        System.out.println("Password in DB: " + user.getPassword());

        boolean passwordMatch = encoder.matches(req.password(), user.getPassword());
        System.out.println("Password coincide: " + passwordMatch);

        var auth = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        authManager.authenticate(auth);

        return buildTokens(user, user.getRoles());
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
