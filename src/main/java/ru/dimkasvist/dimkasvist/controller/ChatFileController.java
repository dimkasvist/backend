package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.dimkasvist.dimkasvist.service.FileStorageService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/files")
@RequiredArgsConstructor
public class ChatFileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<@NonNull Map<String, String>> uploadChatFile(
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        long maxFileSize = 10 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        String fileName = fileStorageService.store(file);
        String fileUrl = fileStorageService.getFileUrl(fileName);

        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("fileUrl", fileUrl);
        response.put("fileType", getFileType(file.getContentType()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String getFileType(String contentType) {
        if (contentType == null) {
            return "FILE";
        }
        
        if (contentType.startsWith("image/")) {
            return "IMAGE";
        } else if (contentType.startsWith("video/")) {
            return "VIDEO";
        } else {
            return "FILE";
        }
    }
}
