package com.foodtruck.backend.presentation.controller;

import com.foodtruck.backend.application.dto.UserDtos.ChangePasswordRequest;
import com.foodtruck.backend.application.dto.UserDtos.ChangePasswordResponse;
import com.foodtruck.backend.application.dto.UserDtos.UpdateAvatarResponse;
import com.foodtruck.backend.application.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Perfil de Usuario", description = "Gestión del perfil del usuario")
public class UserProfileController {

        private final UserProfileService userProfileService;

        @PutMapping("/avatar")
        @Operation(summary = "Actualizar foto de perfil", description = "Permite al usuario cambiar su foto de perfil", responses = {
                        @ApiResponse(responseCode = "200", description = "Avatar actualizado correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Éxito", value = """
                                        {
                                          "message": "Avatar actualizado correctamente",
                                          "avatarUrl": "http://localhost:8081/public/avatars/daniel_12345.jpg",
                                          "username": "daniel",
                                          "timestamp": "2025-10-09T16:30:45.123"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Archivo vacío o formato inválido", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "Archivo vacío", value = """
                                                        {
                                                          "error": "Archivo vacío",
                                                          "message": "El archivo no puede estar vacío",
                                                          "timestamp": "2025-10-09T16:30:45.123"
                                                        }
                                                        """),
                                        @ExampleObject(name = "Formato inválido", value = """
                                                        {
                                                          "error": "Formato de archivo inválido",
                                                          "message": "El archivo debe ser una imagen válida",
                                                          "timestamp": "2025-10-09T16:30:45.123"
                                                        }
                                                        """)
                        })),
                        @ApiResponse(responseCode = "413", description = "Archivo demasiado grande", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Archivo muy grande", value = """
                                        {
                                          "error": "Archivo demasiado grande",
                                          "message": "El archivo no puede exceder los 5MB",
                                          "timestamp": "2025-10-09T16:30:45.123"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Usuario inexistente", value = """
                                        {
                                          "error": "Usuario no encontrado",
                                          "message": "Usuario no encontrado",
                                          "timestamp": "2025-10-09T16:30:45.123"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Sin autenticación", value = """
                                        {
                                          "error": "Unauthorized",
                                          "message": "Token JWT requerido",
                                          "timestamp": "2025-10-09T16:30:45.123"
                                        }
                                        """)))
        })
        public ResponseEntity<UpdateAvatarResponse> updateAvatar(
                        @RequestParam("avatar") MultipartFile file,
                        Authentication authentication) throws IOException {

                String username = authentication.getName();
                UpdateAvatarResponse response = userProfileService.updateAvatar(username, file);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/password")
        @Operation(summary = "Cambiar contraseña", description = "Permite al usuario cambiar su contraseña actual", responses = {
                        @ApiResponse(responseCode = "200", description = "Contraseña cambiada correctamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Éxito", value = """
                                        {
                                          "message": "Contraseña cambiada correctamente",
                                          "username": "daniel",
                                          "timestamp": "2025-10-09T18:30:45.123"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos o contraseñas no coinciden", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "Contraseña actual incorrecta", value = """
                                                        {
                                                          "error": "Contraseña actual incorrecta",
                                                          "message": "La contraseña actual es incorrecta",
                                                          "timestamp": "2025-10-09T18:30:45.123"
                                                        }
                                                        """),
                                        @ExampleObject(name = "Contraseñas no coinciden", value = """
                                                        {
                                                          "error": "Contraseñas no coinciden",
                                                          "message": "Las contraseñas no coinciden",
                                                          "timestamp": "2025-10-09T18:30:45.123"
                                                        }
                                                        """),
                                        @ExampleObject(name = "Nueva contraseña igual", value = """
                                                        {
                                                          "error": "Bad Request",
                                                          "message": "La nueva contraseña debe ser diferente a la actual",
                                                          "timestamp": "2025-10-09T18:30:45.123"
                                                        }
                                                        """)
                        })),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Usuario inexistente", value = """
                                        {
                                          "error": "Usuario no encontrado",
                                          "message": "Usuario no encontrado",
                                          "timestamp": "2025-10-09T18:30:45.123"
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Sin autenticación", value = """
                                        {
                                          "error": "Unauthorized",
                                          "message": "Token JWT requerido",
                                          "timestamp": "2025-10-09T18:30:45.123"
                                        }
                                        """)))
        })
        public ResponseEntity<ChangePasswordResponse> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request,
                        Authentication authentication) {

                String username = authentication.getName();
                ChangePasswordResponse response = userProfileService.changePassword(username, request);
                return ResponseEntity.ok(response);
        }
}