package org.example.bailaconmigo.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    @Value("${backend.url}")
    private String backendUrl;

    private static final Map<String, String> fileHashMap = new HashMap<>();
    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/uploadMedia")
    public String uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            // Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Calculate MD5 hash of the file content
            String fileHash = calculateMD5(file.getBytes());

            // Check if this file has already been uploaded
            if (fileHashMap.containsKey(fileHash)) {
                return fileHashMap.get(fileHash);
            }

            // If it's a new file, proceed with upload
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Escribir el archivo
            Files.write(filePath, file.getBytes());

            // Generar URL accesible - IMPORTANTE: usar /uploads/ no /api/media/uploads
            String fileUrl = backendUrl + "/uploads/" + fileName;

            // Store hash and URL for future reference
            fileHashMap.put(fileHash, fileUrl);

            System.out.println("Archivo subido: " + fileName);
            System.out.println("URL generada: " + fileUrl);

            return fileUrl;
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Error subiendo archivo: " + e.getMessage());
            throw new RuntimeException("Error al subir archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate MD5 hash of file content
     */
    private String calculateMD5(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(fileBytes);
        return Base64.getEncoder().encodeToString(digest);
    }

}
