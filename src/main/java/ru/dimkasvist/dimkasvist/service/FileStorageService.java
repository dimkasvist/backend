package ru.dimkasvist.dimkasvist.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file);

    byte[] load(String fileName);

    String getFileUrl(String fileName);

    void delete(String fileName);
}
