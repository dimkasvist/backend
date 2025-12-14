package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.dimkasvist.dimkasvist.config.S3Properties;
import ru.dimkasvist.dimkasvist.exception.FileStorageException;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.service.FileStorageService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public String store(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFileName.contains("..")) {
            throw new FileStorageException("Invalid file path: " + originalFileName);
        }

        String fileExtension = getFileExtension(originalFileName);
        String newFileName = UUID.randomUUID() + fileExtension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(newFileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return newFileName;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalFileName, e);
        } catch (S3Exception e) {
            throw new FileStorageException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] load(String fileName) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileName)
                    .build();

            return s3Client.getObjectAsBytes(getRequest).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        } catch (S3Exception e) {
            throw new FileStorageException("Failed to load file from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        String baseUrl = s3Properties.getPublicUrl() != null && !s3Properties.getPublicUrl().isEmpty()
                ? s3Properties.getPublicUrl()
                : s3Properties.getEndpoint();
        return baseUrl + "/" + s3Properties.getBucket() + "/" + fileName;
    }

    @Override
    public void delete(String fileName) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            throw new FileStorageException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }
}
