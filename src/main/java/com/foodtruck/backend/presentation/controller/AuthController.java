package com.foodtruck.backend.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.foodtruck.backend.application.dto.AuthDtos.AuthRequest;
import com.foodtruck.backend.application.dto.AuthDtos.AuthResponse;
import com.foodtruck.backend.application.dto.AuthDtos.LogoutResponse;
import com.foodtruck.backend.application.dto.AuthDtos.RefreshRequest;
import com.foodtruck.backend.application.dto.AuthDtos.RegisterRequest;
import com.foodtruck.backend.application.dto.AuthDtos.RoleVerificationResponse;
import com.foodtruck.backend.application.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints relacionados con el registro, inicio de sesión y actualización de tokens JWT.")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Registrar un nuevo usuario", description = """
      Crea un nuevo usuario con las credenciales proporcionadas.
      Devuelve un token de acceso JWT si el registro es exitoso.
      Si el usuario o email ya existen, o si la validación de datos falla, devuelve un error detallado.
      """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Datos de registro del usuario", content = @Content(schema = @Schema(implementation = RegisterRequest.class), examples = @ExampleObject(name = "Ejemplo de registro", value = """
      {
        "username": "daniel",
        "email": "daniel@example.com",
        "password": "Abc123$%",
        "roles": ["ROLE_USER"]
      }
      """))), responses = {
      @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name = "Registro exitoso", value = """
          {
            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "tokenType": "Bearer",
            "expiresIn": 3600000
          }
          """))),

      @ApiResponse(responseCode = "409", description = "El usuario o email ya existen", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Usuario duplicado", value = """
          {
            "timestamp": "2025-10-05T19:15:41.740694500",
            "error": "El usuario o email ya existe"
          }
          """))),

      @ApiResponse(responseCode = "400", description = "Datos inválidos o fallos de validación", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Error de validación", value = """
          {
            "error": "Datos de entrada inválidos",
            "validationErrors": {
              "username": "El nombre de usuario debe tener entre 3 y 20 caracteres"
            },
            "timestamp": "2025-10-05T19:17:53.673924500"
          }
          """)))
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
      @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Error de autenticación", value = """
          {
            "error": "Credenciales inválidas"
          }
          """)))
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

  @PostMapping("/logout")
  @Operation(summary = "Cerrar sesión", description = """
      Invalida el token JWT actual agregándolo a una lista negra.

      **Autenticación requerida:** Este endpoint requiere un token JWT válido en el header Authorization.

      **Formato del header:**
      ```
      Authorization: Bearer <token-jwt>
      ```
      """, security = @SecurityRequirement(name = "bearerAuth"), responses = {
      @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Logout exitoso", value = """
          {
            "message": "Sesión cerrada correctamente",
            "timestamp": "2025-10-09T15:30:45.123"
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Token JWT inválido, expirado o no proporcionado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Sin autorización", value = """
          {
            "error": "Unauthorized",
            "message": "Token JWT requerido o inválido",
            "timestamp": "2025-10-18T15:30:45.123"
          }
          """)))
  })
  public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
    authService.logout(authHeader);
    return ResponseEntity.ok(new LogoutResponse("Sesión cerrada correctamente"));
  }

  @GetMapping("/verify-roles")
  @Operation(summary = "Verificar roles del usuario", description = """
      Obtiene los roles del usuario autenticado basado en el token JWT.

      **Autenticación requerida:** Este endpoint requiere un token JWT válido en el header Authorization.

      **Formato del header:**
      ```
      Authorization: Bearer <token-jwt>
      ```
      """, security = @SecurityRequirement(name = "bearerAuth"), responses = {
      @ApiResponse(responseCode = "200", description = "Roles obtenidos correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Roles del usuario", value = """
          {
            "username": "daniel",
            "roles": ["ROLE_USER", "ROLE_ADMIN"],
            "timestamp": "2025-10-09T15:30:45.123"
          }
          """))),
      @ApiResponse(responseCode = "401", description = "Token JWT inválido, expirado o no proporcionado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Sin autorización", value = """
          {
            "error": "Unauthorized",
            "message": "Token JWT requerido o inválido",
            "timestamp": "2025-10-18T15:30:45.123"
          }
          """)))
  })
  public ResponseEntity<RoleVerificationResponse> verifyRoles(Authentication authentication) {
    String username = authentication.getName();
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    Set<String> roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    return ResponseEntity.ok(new RoleVerificationResponse(username, roles));
  }
}