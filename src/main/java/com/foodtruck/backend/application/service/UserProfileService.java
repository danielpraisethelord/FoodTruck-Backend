package com.foodtruck.backend.application.service;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.foodtruck.backend.application.dto.UserDtos.ChangePasswordRequest;
import com.foodtruck.backend.application.dto.UserDtos.ChangePasswordResponse;
import com.foodtruck.backend.application.dto.UserDtos.UpdateAvatarResponse;
import com.foodtruck.backend.application.dto.UserDtos.UserProfileResponse;
import com.foodtruck.backend.domain.repository.UserRepository;
import com.foodtruck.backend.presentation.exception.file.FileExceptions;
import com.foodtruck.backend.presentation.exception.user.UserExceptions;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder encoder;

    @Transactional
    public UpdateAvatarResponse updateAvatar(String username, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new FileExceptions.EmptyFileException("El archivo no puede estar vacío");
        }

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new FileExceptions.UserNotFoundException("Usuario no encontrado"));

        if (user.getAvatar() != null) {
            fileStorageService.deleteFile(user.getAvatar());
        }

        String avatarUrl = fileStorageService.saveAvatar(file, username);
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return new UpdateAvatarResponse(
                "Avatar actualizado correctamente",
                avatarUrl,
                username);
    }

    @Transactional
    public ChangePasswordResponse changePassword(String username, ChangePasswordRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new FileExceptions.UserNotFoundException("Usuario no encontrado"));

        if (!encoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UserExceptions.InvalidCurrentPasswordException("La contraseña actual es incorrecta");
        }

        if (encoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new UserExceptions.PasswordMismatchException("Las contraseñas no coinciden");
        }

        user.setPassword(encoder.encode(request.newPassword()));
        userRepository.save(user);

        return new ChangePasswordResponse("Contraseña cambiada correctamente", username);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new FileExceptions.UserNotFoundException("Usuario no encontrado"));

        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getAvatar(),
                user.getRegisterDate(),
                roleNames);
    }
}
