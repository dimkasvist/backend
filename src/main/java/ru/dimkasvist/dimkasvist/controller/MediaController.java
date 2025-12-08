package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.dimkasvist.dimkasvist.dto.FeedResponse;
import ru.dimkasvist.dimkasvist.dto.MediaResponse;
import ru.dimkasvist.dimkasvist.dto.MediaUploadRequest;
import ru.dimkasvist.dimkasvist.service.FeedService;
import ru.dimkasvist.dimkasvist.service.MediaService;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final FeedService feedService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<@NonNull MediaResponse> uploadMedia(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description
    ) {
        MediaUploadRequest request = new MediaUploadRequest();
        request.setTitle(title);
        request.setDescription(description);

        MediaResponse response = mediaService.upload(file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull MediaResponse> getMedia(@PathVariable Long id) {
        MediaResponse response = mediaService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed")
    public ResponseEntity<@NonNull FeedResponse> getFeed(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        FeedResponse response = feedService.getFeed(cursor, Math.min(size, 50));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NonNull Void> deleteMedia(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
