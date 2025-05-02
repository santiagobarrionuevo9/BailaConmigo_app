package org.example.bailaconmigo.Controllers;

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
    // Map to store file hashes and their URLs
    private static final Map<String, String> fileHashMap = new HashMap<>();

    @PostMapping("/uploadMedia")
    public String uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            // Calculate MD5 hash of the file content
            String fileHash = calculateMD5(file.getBytes());

            // Check if this file has already been uploaded
            if (fileHashMap.containsKey(fileHash)) {
                // Return the existing URL instead of uploading again
                return fileHashMap.get(fileHash);
            }

            // If it's a new file, proceed with upload
            String uploadDir = "uploads/";
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            String fileUrl = "http://localhost:8080/" + uploadDir + fileName;

            // Store hash and URL for future reference
            fileHashMap.put(fileHash, fileUrl);

            return fileUrl;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al subir archivo", e);
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
