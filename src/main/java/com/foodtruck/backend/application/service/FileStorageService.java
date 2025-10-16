package com.foodtruck.backend.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.foodtruck.backend.presentation.exception.product.ProductExceptions.InvalidProductImageException;
import com.foodtruck.backend.presentation.exception.product.ProductExceptions.ProductImageProcessingException;
import com.foodtruck.backend.presentation.exception.product.ProductExceptions.ProductImageTooLargeException;

@Service
public class FileStorageService {

    @Value("${app.upload.dir.avatar}")
    private String uploadDir;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.upload.dir.product}")
    private String productUploadDir;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    public String saveAvatar(MultipartFile file, String username) throws IOException {

        Path uploadPath = Paths.get("src/main/resources/" + uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = username + "_" + UUID.randomUUID().toString() + fileExtension;

        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + "/avatars/" + newFilename;
    }

    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl != null && avatarUrl.startsWith(baseUrl)) {
            try {
                String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir, filename);
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                System.err.println("Errror deleting file: " + e.getMessage());
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Guarda una imagen de producto en el sistema de archivos
     * 
     * @param file        Archivo de imagen a guardar
     * @param productName Nombre del producto (para generar nombre único)
     * @return URL pública de la imagen guardada
     * @throws InvalidProductImageException    si el archivo no es una imagen válida
     * @throws ProductImageTooLargeException   si el archivo excede el tamaño máximo
     * @throws ProductImageProcessingException si ocurre un error al procesar la
     *                                         imagen
     */
    public String saveProductImage(MultipartFile file, String productName) {
        validateProductImage(file);

        try {
            Path uploadPath = Paths.get("src/main/resources/" + productUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String sanitizedProductName = sanitizeFilename(productName);
            String newFilename = sanitizedProductName + "_" + UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return baseUrl + "/products/" + newFilename;

        } catch (IOException e) {
            throw new ProductImageProcessingException("Error al guardar la imagen del producto", e);
        }
    }

    /**
     * Elimina una imagen de producto del sistema de archivos
     * 
     * @param imageUrl URL de la imagen a eliminar
     */
    public void deleteProductImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith(baseUrl)) {
            try {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get("src/main/resources/" + productUploadDir, filename);
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                System.err.println("Error eliminando imagen del producto: " + e.getMessage());
            }
        }
    }

    /**
     * Valida que el archivo sea una imagen válida para productos
     * 
     * @param file Archivo a validar
     * @throws InvalidProductImageException  si el archivo no es válido
     * @throws ProductImageTooLargeException si el archivo es muy grande
     */
    private void validateProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidProductImageException("No se proporcionó ningún archivo de imagen");
        }

        if (!isValidImageFile(file)) {
            throw new InvalidProductImageException("El archivo debe ser una imagen válida (JPG, PNG, GIF, etc.)");
        }

        if (file.getSize() > maxFileSize) {
            throw new ProductImageTooLargeException(maxFileSize);
        }
    }

    /**
     * Sanitiza el nombre del archivo para evitar caracteres problemáticos
     * 
     * @param filename Nombre a sanitizar
     * @return Nombre sanitizado
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "product";
        }

        return filename.toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "_")
                .replaceAll("_{2,}", "_")
                .substring(0, Math.min(filename.length(), 50));
    }
}
