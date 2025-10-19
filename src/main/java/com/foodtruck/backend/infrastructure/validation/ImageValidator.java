package com.foodtruck.backend.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private boolean required;
    private long maxSize;
    private List<String> allowedTypes;

    @Override
    public void initialize(ValidImage constraintAnnotation) {
        this.required = constraintAnnotation.required();
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = Arrays.asList(constraintAnnotation.allowedTypes());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Si el archivo es nulo o vacío
        if (file == null || file.isEmpty()) {
            if (required) {
                addConstraintViolation(context, "La imagen es obligatoria");
                return false;
            }
            return true; // Si no es requerido y está vacío, es válido
        }

        // Validar tamaño
        if (file.getSize() > maxSize) {
            addConstraintViolation(context, "La imagen no puede exceder " + (maxSize / (1024 * 1024)) + "MB");
            return false;
        }

        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            addConstraintViolation(context, "El archivo debe ser una imagen válida (JPEG, PNG, GIF, WebP)");
            return false;
        }

        // Validar extensión del archivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
            if (!allowedExtensions.contains(extension)) {
                addConstraintViolation(context, "La extensión del archivo no es válida");
                return false;
            }
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
}