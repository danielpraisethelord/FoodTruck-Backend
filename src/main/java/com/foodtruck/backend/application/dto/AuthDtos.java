package com.foodtruck.backend.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Set;
import com.foodtruck.backend.domain.model.Role;

@Schema(description = "Conjunto de DTOs utilizados en el proceso de autenticación y registro de usuarios.")
public class AuthDtos {

        @Schema(description = "Petición para registrar un nuevo usuario en el sistema.")
        public record RegisterRequest(

                        @Schema(description = "Nombre de usuario único. Solo se permiten letras, números y guiones bajos.", example = "daniel123", minLength = 3, maxLength = 20, pattern = "^[a-zA-Z0-9_]+$") @NotBlank(message = "El nombre de usuario es obligatorio") @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres") @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El nombre de usuario solo puede contener letras, números y guiones bajos") String username,

                        @Schema(description = "Correo electrónico válido del usuario.", example = "daniel@example.com") @NotBlank(message = "El email es obligatorio") @Email(message = "El formato del email no es válido") String email,

                        @Schema(description = """
                                        Contraseña del usuario. Debe contener al menos una letra minúscula, una mayúscula,
                                        un número y un carácter especial. Longitud mínima de 8 caracteres.
                                        """, example = "Abc123$%", minLength = 8, maxLength = 100, pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$") @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$", message = "La contraseña debe contener al menos una letra minúscula, una mayúscula, un número y un carácter especial") String password,

                        @Schema(description = "Nombre completo del usuario.", example = "Daniel Santiago", maxLength = 100) @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres") @NotBlank(message = "El nombre es obligatorio") String name,

                        @Schema(description = "Conjunto de roles asignados al nuevo usuario.", example = "[\"ROLE_USER\"]", allowableValues = {
                                        "ROLE_USER", "ROLE_ADMIN", "ROLE_EMPLOYEE" }) Set<Role> roles){
        }

        @Schema(description = "Petición para autenticar a un usuario existente.")
        public record AuthRequest(

                        @Schema(description = "Nombre de usuario registrado.", example = "daniel123") @NotBlank(message = "El nombre de usuario es obligatorio") String username,

                        @Schema(description = "Contraseña correspondiente al usuario.", example = "Abc123$%") @NotBlank(message = "La contraseña es obligatoria") String password) {
        }

        @Schema(description = "Petición para refrescar el token JWT de acceso.")
        public record RefreshRequest(

                        @Schema(description = "Token de refresco emitido durante el inicio de sesión.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") @NotBlank(message = "El token de refresco es obligatorio") String refreshToken) {
        }

        @Schema(description = "Respuesta devuelta tras un registro, login o refresh exitoso.")
        public record AuthResponse(

                        @Schema(description = "Token JWT de acceso generado para el usuario.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,

                        @Schema(description = "Token JWT de refresco, utilizado para generar nuevos tokens de acceso.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String refreshToken,

                        @Schema(description = "Tipo de token devuelto. Generalmente 'Bearer'.", example = "Bearer") String tokenType,

                        @Schema(description = "Tiempo de expiración del token de acceso en milisegundos.", example = "3600000") long expiresInMs) {
        }

        public record LogoutResponse(
                        @Schema(description = "Mensaje que confirma el cierre de sesión.", example = "Logout successful") String message,
                        @Schema(description = "Marca de tiempo del cierre de sesión.", example = "2023-03-15T12:34:56") LocalDateTime timestamp) {
                public LogoutResponse(String message) {
                        this(message, LocalDateTime.now());
                }
        }

        public record RoleVerificationResponse(
                        @Schema(description = "Nombre de usuario verificado.", example = "daniel123") String username,
                        @Schema(description = "Conjunto de roles asignados al usuario.", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]") Set<String> roles,
                        @Schema(description = "Marca de tiempo de la verificación.", example = "2023-03-15T12:34:56") LocalDateTime timestamp) {
                public RoleVerificationResponse(String username, Set<String> roles) {
                        this(username, roles, LocalDateTime.now());
                }
        }
}
