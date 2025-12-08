package ru.dimkasvist.dimkasvist.service;

import org.springframework.web.multipart.MultipartFile;
import ru.dimkasvist.dimkasvist.dto.MediaResponse;
import ru.dimkasvist.dimkasvist.dto.MediaUploadRequest;

public interface MediaService {

    MediaResponse upload(MultipartFile file, MediaUploadRequest request);

    MediaResponse getById(Long id);

    void delete(Long id);
}
