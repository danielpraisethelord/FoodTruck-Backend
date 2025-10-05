package com.foodtruck.backend.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foodtruck.backend.application.dto.AuthDtos.AuthRequest;
import com.foodtruck.backend.application.dto.AuthDtos.AuthResponse;
import com.foodtruck.backend.application.dto.AuthDtos.RefreshRequest;
import com.foodtruck.backend.application.dto.AuthDtos.RegisterRequest;
import com.foodtruck.backend.application.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints relacionados con el registro, inicio de sesión y actualización de tokens JWT.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un nuevo usuario con las credenciales proporcionadas. Devuelve un token de acceso JWT.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Datos de registro del usuario", content = @Content(schema = @Schema(implementation = RegisterRequest.class), examples = @ExampleObject(name = "Ejemplo de registro", value = """
            {
              "username": "daniel",
              "email": "daniel@example.com",
              "password": "123456",
              "roles": ["ROLE_USER"]
            }
            """))), responses = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existente", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario con sus credenciales y devuelve un token de acceso y uno de refresco.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Credenciales del usuario", content = @Content(schema = @Schema(implementation = AuthRequest.class), examples = @ExampleObject(name = "Ejemplo de login", value = """
            {
              "username": "daniel",
              "password": "123456"
            }
            """))), responses = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token JWT", description = "Genera un nuevo token de acceso usando el token de refresco válido.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Token de refresco", content = @Content(schema = @Schema(implementation = RefreshRequest.class), examples = @ExampleObject(name = "Ejemplo de refresh", value = """
            {
              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
            """))), responses = {
            @ApiResponse(responseCode = "200", description = "Token refrescado correctamente", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token de refresco inválido o expirado")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }
}