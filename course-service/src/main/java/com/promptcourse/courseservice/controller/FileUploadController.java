package com.promptcourse.courseservice.controller;

import com.promptcourse.courseservice.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    // Принимает файл, возвращает одну готовую ссылку
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.uploadFile(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // Удаляет файл по ссылке
    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam String fileUrl) {
        fileStorageService.deleteFile(fileUrl);
        return ResponseEntity.noContent().build();
    }
}