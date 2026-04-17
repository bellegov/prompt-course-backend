package com.promptcourse.courseservice.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AmazonS3 s3Client;

    @Value("${r2.bucket-name}") private String bucketName;
    @Value("${r2.public-url}") private String publicUrl;

    // Загружает файл в R2, возвращает готовую публичную ссылку
    public String uploadFile(MultipartFile file) {
        String fileKey = "media/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            s3Client.putObject(bucketName, fileKey, file.getInputStream(), metadata);
            log.info("Successfully uploaded file: {}", fileKey);
            return publicUrl + "/" + fileKey;
        } catch (Exception e) {
            log.error("Failed to upload file: {}", fileKey, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    // Удаляет файл из R2 по публичной ссылке
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith(publicUrl)) {
            log.warn("Invalid or non-R2 URL: {}", fileUrl);
            return;
        }
        try {
            String fileKey = fileUrl.substring(publicUrl.length() + 1);
            s3Client.deleteObject(bucketName, fileKey);
            log.info("Successfully deleted file: {}", fileKey);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }
}