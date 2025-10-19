package com.foodtruck.backend.presentation.controller;

import com.foodtruck.backend.application.dto.UserDtos;
import com.foodtruck.backend.application.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
  @Operation(summary = "Actualizar foto de perfil", description = """
      Permite al usuario cambiar su foto de perfil mediante el envío de un archivo de imagen.
      El archivo debe enviarse como multipart/form-data con la clave 'avatar'.
      Formatos soportados: JPG, JPEG, PNG, GIF.
      Tamaño máximo: 5MB.

      **Autenticación requerida:** Este endpoint requiere un token JWT válido en el header Authorization.

      **Formato del header:**
      ```
      Authorization: Bearer <token-jwt>
      ```
      """, security = @SecurityRequirement(name = "bearerAuth"), requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Archivo de imagen para el avatar del usuario", content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object", requiredProperties = {
      "avatar" }), examples = @ExampleObject(name = "Subir avatar", description = "Selecciona un archivo de imagen (JPG, JPEG, PNG, GIF) con el campo 'avatar'"))), responses = {
          @ApiResponse(responseCode = "200", description = "Avatar actualizado correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDtos.UpdateAvatarResponse.class), examples = @ExampleObject(name = "Éxito", value = """
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
                    "message": "El archivo debe ser una imagen válida (JPG, JPEG, PNG, GIF)",
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
  public ResponseEntity<UserDtos.UpdateAvatarResponse> updateAvatar(
      @RequestParam("avatar") MultipartFile file,
      Authentication authentication) throws IOException {

    String username = authentication.getName();
    UserDtos.UpdateAvatarResponse response = userProfileService.updateAvatar(username, file);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/password")
  @Operation(summary = "Cambiar contraseña", description = """
      Permite al usuario cambiar su contraseña actual.

      **Autenticación requerida:** Este endpoint requiere un token JWT válido en el header Authorization.

      **Formato del header:**
      ```
      Authorization: Bearer <token-jwt>
      ```
      """, security = @SecurityRequirement(name = "bearerAuth"), responses = {
      @ApiResponse(responseCode = "200", description = "Contraseña cambiada correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDtos.ChangePasswordResponse.class), examples = @ExampleObject(name = "Éxito", value = """
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
  public ResponseEntity<UserDtos.ChangePasswordResponse> changePassword(
      @Valid @RequestBody UserDtos.ChangePasswordRequest request,
      Authentication authentication) {

    String username = authentication.getName();
    UserDtos.ChangePasswordResponse response = userProfileService.changePassword(username, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "Obtener perfil de usuario", description = """
      Obtiene la información del perfil del usuario autenticado.

      **Autenticación requerida:** Este endpoint requiere un token JWT válido en el header Authorization.

      **Formato del header:**
      ```
      Authorization: Bearer <token-jwt>
      ```
      """, security = @SecurityRequirement(name = "bearerAuth"), responses = {
      @ApiResponse(responseCode = "200", description = "Información del usuario obtenida correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDtos.UserProfileResponse.class), examples = @ExampleObject(name = "Éxito", value = """
          {
            "id": 1,
            "username": "daniel",
            "email": "daniel@example.com",
            "name": "Daniel Santiago",
            "avatarUrl": "http://localhost:8081/public/avatars/daniel_12345.jpg",
            "registerDate": "2023-03-15T12:34:56.789",
            "roles": ["USER"]
          }
          """))),
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
  public ResponseEntity<UserDtos.UserProfileResponse> getUserProfile(Authentication authentication) {
    String username = authentication.getName();
    UserDtos.UserProfileResponse response = userProfileService.getUserProfile(username);
    return ResponseEntity.ok(response);
  }
}