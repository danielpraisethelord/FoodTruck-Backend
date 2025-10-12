package com.foodtruck.backend.application.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Conjunto de DTOs relacionados con las operaciones de usuario.")
public class UserDtos {

    @Schema(description = "Respuesta devuelta tras la actualización del avatar de un usuario.")
    public record UpdateAvatarResponse(

            @Schema(description = "Mensaje que confirma la actualización del avatar.", example = "Avatar actualizado correctamente") String message,
            @Schema(description = "URL del nuevo avatar del usuario.", example = "http://localhost:8081/public/avatars/user123.jpg") String avatarUrl,
            @Schema(description = "Nombre de usuario al que pertenece el avatar actualizado.", example = "user123") String username,
            @Schema(description = "Marca de tiempo de la actualización del avatar.", example = "2023-03-15T12:34:56") LocalDateTime timestamp) {
        public UpdateAvatarResponse(String message, String avatarUrl, String username) {
            this(message, avatarUrl, username, LocalDateTime.now());
        }
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "La contraseña actual es requerida") String currentPassword,

            @NotBlank(message = "La nueva contraseña es requerida") @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres") String newPassword,

            @NotBlank(message = "La confirmación de contraseña es requerida") String confirmNewPassword) {
        public ChangePasswordRequest {

        }
    }

    public record ChangePasswordResponse(
            String message,
            String username,
            LocalDateTime timestamp) {
        public ChangePasswordResponse(String message, String username) {
            this(message, username, LocalDateTime.now());
        }
    }
}
