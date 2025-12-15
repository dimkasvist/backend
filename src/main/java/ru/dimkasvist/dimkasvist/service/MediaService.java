package ru.dimkasvist.dimkasvist.service;

import org.springframework.web.multipart.MultipartFile;
import ru.dimkasvist.dimkasvist.dto.MediaResponse;
import ru.dimkasvist.dimkasvist.dto.MediaUploadRequest;
import ru.dimkasvist.dimkasvist.dto.MediaUpdateRequest;

public interface MediaService {

    MediaResponse upload(MultipartFile file, MediaUploadRequest request);

    MediaResponse getById(Long id);

    MediaResponse update(Long id, MediaUpdateRequest request);

    void delete(Long id);
}
