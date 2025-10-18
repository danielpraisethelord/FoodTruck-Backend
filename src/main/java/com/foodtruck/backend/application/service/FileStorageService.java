package com.foodtruck.backend.application.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.foodtruck.backend.presentation.exception.product.ProductExceptions.InvalidProductImageException;
import com.foodtruck.backend.presentation.exception.product.ProductExceptions.ProductImageProcessingException;
import com.foodtruck.backend.presentation.exception.product.ProductExceptions.ProductImageTooLargeException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Storage storage;
    private final String firebaseBucketName;

    // ------------------------------------------------------------
    // MÉTODOS PÚBLICOS
    // ------------------------------------------------------------

    /**
     * Sube y optimiza una imagen de avatar al bucket de Firebase.
     *
     * @param file     Imagen recibida desde el cliente.
     * @param username Nombre de usuario (usado para nombrar el archivo).
     * @return URL pública estable de la imagen subida.
     * @throws InvalidProductImageException    Si el archivo no es una imagen
     *                                         válida.
     * @throws ProductImageTooLargeException   Si el archivo excede el tamaño máximo
     *                                         permitido.
     * @throws ProductImageProcessingException Si ocurre un error al procesar o
     *                                         subir la imagen.
     */
    public String saveAvatar(MultipartFile file, String username) {
        return uploadToFirebase(file, "avatars", username);
    }

    /**
     * Sube y optimiza una imagen de producto al bucket de Firebase.
     *
     * @param file        Imagen del producto.
     * @param productName Nombre del producto (usado para nombrar el archivo).
     * @return URL pública estable de la imagen subida.
     * @throws InvalidProductImageException    Si el archivo no es una imagen
     *                                         válida.
     * @throws ProductImageTooLargeException   Si el archivo excede el tamaño máximo
     *                                         permitido.
     * @throws ProductImageProcessingException Si ocurre un error al procesar o
     *                                         subir la imagen.
     */
    public String saveProductImage(MultipartFile file, String productName) {
        return uploadToFirebase(file, "products", productName);
    }

    /**
     * Elimina una imagen del bucket de Firebase Storage a partir de su URL pública.
     *
     * @param imageUrl URL pública de la imagen a eliminar.
     */
    public void deleteFile(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isBlank())
                return;

            String filename = imageUrl.substring(imageUrl.indexOf("/o/") + 3, imageUrl.indexOf("?alt=media"));
            filename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);

            BlobId blobId = BlobId.of(firebaseBucketName, filename);
            storage.delete(blobId);

        } catch (Exception e) {
            System.err.println("Error eliminando archivo: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // MÉTODOS PRIVADOS
    // ------------------------------------------------------------

    /**
     * Sube un archivo al bucket de Firebase tras validarlo y optimizarlo.
     *
     * @param file     Archivo de imagen recibido.
     * @param folder   Carpeta de destino en el bucket ("avatars" o "products").
     * @param baseName Nombre base para el archivo (usuario o producto).
     * @return URL pública generada por Firebase Storage.
     */
    private String uploadToFirebase(MultipartFile file, String folder, String baseName) {
        validateImage(file);

        try {
            byte[] optimizedBytes = optimizeImage(file.getBytes(), file.getContentType(), folder);

            String sanitizedBaseName = sanitizeFilename(baseName);
            String extension = getFileExtension(file.getOriginalFilename());
            String newFilename = folder + "/" + sanitizedBaseName + "_" + UUID.randomUUID() + extension;

            BlobId blobId = BlobId.of(firebaseBucketName, newFilename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, optimizedBytes);

            return String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    firebaseBucketName,
                    URLEncoder.encode(newFilename, StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new ProductImageProcessingException("Error al subir la imagen a Firebase", e);
        }
    }

    /**
     * Valida que el archivo sea una imagen válida y que cumpla con el tamaño máximo
     * permitido.
     *
     * @param file Archivo a validar.
     * @throws InvalidProductImageException  Si el tipo MIME no corresponde a una
     *                                       imagen.
     * @throws ProductImageTooLargeException Si el tamaño excede los 5 MB.
     */
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidProductImageException("No se proporcionó ningún archivo de imagen");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidProductImageException("El archivo debe ser una imagen válida (JPG, PNG, WEBP, etc.)");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ProductImageTooLargeException(5 * 1024 * 1024);
        }
    }

    /**
     * Redimensiona y comprime una imagen para optimizar su almacenamiento en
     * Firebase.
     *
     * @param imageBytes  Bytes originales de la imagen.
     * @param contentType Tipo MIME del archivo.
     * @param folder      Carpeta de destino (para determinar dimensiones máximas).
     * @return Bytes de la imagen optimizada.
     * @throws IOException Si ocurre un error de lectura o escritura.
     */
    private byte[] optimizeImage(byte[] imageBytes, String contentType, String folder) throws IOException {
        int maxWidth = folder.equals("avatars") ? 256 : 800;
        int maxHeight = folder.equals("avatars") ? 256 : 800;

        try (InputStream in = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            BufferedImage original = ImageIO.read(in);
            if (original == null)
                return imageBytes;

            int width = original.getWidth();
            int height = original.getHeight();

            if (width > maxWidth || height > maxHeight) {
                float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
                int newW = Math.round(width * scale);
                int newH = Math.round(height * scale);

                Image scaled = original.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(scaled, 0, 0, null);
                g2d.dispose();
                original = resized;
            }

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext())
                return imageBytes;
            ImageWriter writer = writers.next();

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.7f);
                }
                writer.write(null, new IIOImage(original, null, null), param);
            } finally {
                writer.dispose();
            }

            return out.toByteArray();
        }
    }

    /**
     * Obtiene la extensión de un archivo a partir de su nombre.
     *
     * @param filename Nombre del archivo original.
     * @return Extensión del archivo (por defecto ".jpg").
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Normaliza un nombre para que sea seguro como nombre de archivo.
     *
     * @param name Nombre base a sanitizar.
     * @return Nombre seguro y en minúsculas.
     */
    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank())
            return "image";

        String sanitized = name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\-_]", "_")
                .replaceAll("_{2,}", "_");

        if (sanitized.isBlank())
            sanitized = "image";

        return sanitized.substring(0, Math.min(sanitized.length(), 50));
    }

}
