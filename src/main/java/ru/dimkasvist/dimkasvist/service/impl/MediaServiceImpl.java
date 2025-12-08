package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.dimkasvist.dimkasvist.dto.MediaResponse;
import ru.dimkasvist.dimkasvist.dto.MediaUploadRequest;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.FileStorageException;
import ru.dimkasvist.dimkasvist.exception.ForbiddenException;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.mapper.MediaMapper;
import ru.dimkasvist.dimkasvist.repository.MediaRepository;
import ru.dimkasvist.dimkasvist.service.FileStorageService;
import ru.dimkasvist.dimkasvist.service.MediaService;
import ru.dimkasvist.dimkasvist.service.UserService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/webm", "video/x-matroska"
    );

    private final MediaRepository mediaRepository;
    private final FileStorageService fileStorageService;
    private final MediaMapper mediaMapper;
    private final UserService userService;

    @Override
    @Transactional
    public MediaResponse upload(MultipartFile file, MediaUploadRequest request) {
        validateFile(file);

        User currentUser = userService.getCurrentUser();
        String fileName = fileStorageService.store(file);

        Media.MediaBuilder builder = Media.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .fileName(fileName)
                .filePath(fileStorageService.getFileUrl(fileName))
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .user(currentUser);

        if (isImage(file.getContentType())) {
            ImageDimensions dimensions = getImageDimensions(file);
            builder.width(dimensions.width()).height(dimensions.height());
        } else {
            builder.width(null).height(null);
            // durationSeconds можно заполнить позже при интеграции видеопарсера
        }

        Media saved = mediaRepository.save(builder.build());
        return mediaMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MediaResponse getById(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        return mediaMapper.toResponse(media);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));

        User currentUser = userService.getCurrentUser();
        if (!media.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own media");
        }

        fileStorageService.delete(media.getFileName());
        mediaRepository.delete(media);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileStorageException("Content-Type is required");
        }
        if (!isImage(contentType) && !isVideo(contentType)) {
            throw new FileStorageException("File type not allowed. Allowed types: " + ALLOWED_IMAGE_TYPES + " " + ALLOWED_VIDEO_TYPES);
        }
    }

    private boolean isImage(String contentType) {
        return ALLOWED_IMAGE_TYPES.contains(contentType);
    }

    private boolean isVideo(String contentType) {
        return ALLOWED_VIDEO_TYPES.contains(contentType);
    }

    private ImageDimensions getImageDimensions(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new FileStorageException("Cannot read image file");
            }
            return new ImageDimensions(image.getWidth(), image.getHeight());
        } catch (IOException e) {
            throw new FileStorageException("Failed to read image dimensions", e);
        }
    }

    private record ImageDimensions(int width, int height) {}
}
