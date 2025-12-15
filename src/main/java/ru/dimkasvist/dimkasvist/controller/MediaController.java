package ru.dimkasvist.dimkasvist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.dimkasvist.dimkasvist.dto.FeedResponse;
import ru.dimkasvist.dimkasvist.dto.MediaResponse;
import ru.dimkasvist.dimkasvist.dto.MediaUpdateRequest;
import ru.dimkasvist.dimkasvist.dto.MediaUploadRequest;
import ru.dimkasvist.dimkasvist.dto.SearchResponse;
import ru.dimkasvist.dimkasvist.service.FeedService;
import ru.dimkasvist.dimkasvist.service.MediaService;
import ru.dimkasvist.dimkasvist.service.SearchService;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final FeedService feedService;
    private final SearchService searchService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<@NonNull MediaResponse> uploadMedia(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "tags", required = false) String tags
    ) {
        MediaUploadRequest request = new MediaUploadRequest();
        request.setTitle(title);
        request.setDescription(description);
        
        if (tags != null && !tags.isBlank()) {
            Set<String> tagSet = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
            request.setTags(tagSet);
        }

        MediaResponse response = mediaService.upload(file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull MediaResponse> getMedia(@PathVariable Long id) {
        MediaResponse response = mediaService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull MediaResponse> updateMedia(
            @PathVariable Long id,
            @Valid @RequestBody MediaUpdateRequest request
    ) {
        MediaResponse response = mediaService.update(id, request);
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

    @GetMapping("/search")
    public ResponseEntity<@NonNull SearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchResponse response = searchService.search(q, size);
        return ResponseEntity.ok(response);
    }
}
