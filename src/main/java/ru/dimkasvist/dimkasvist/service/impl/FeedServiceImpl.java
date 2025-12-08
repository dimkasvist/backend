package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.FeedItemResponse;
import ru.dimkasvist.dimkasvist.dto.FeedResponse;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.mapper.MediaMapper;
import ru.dimkasvist.dimkasvist.repository.MediaRepository;
import ru.dimkasvist.dimkasvist.service.FeedService;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private static final Comparator<FeedItemResponse> FEED_COMPARATOR = Comparator
            .comparing(FeedItemResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(FeedItemResponse::getId, Comparator.nullsLast(Comparator.reverseOrder()));

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    @Transactional(readOnly = true)
    public FeedResponse getFeed(String cursor, int size) {
        CursorData cursorData = cursor == null || cursor.isBlank() ? null : decodeCursor(cursor);

        int fetchSize = size + 1;
        List<Media> mediaList = cursorData == null
                ? mediaRepository.findFeedInitial(org.springframework.data.domain.PageRequest.of(0, fetchSize))
                : mediaRepository.findFeedAfterCursor(cursorData.createdAt(), cursorData.id(), org.springframework.data.domain.PageRequest.of(0, fetchSize));

        List<FeedItemResponse> items = mediaList.stream()
                .map(mediaMapper::toFeedItem)
                .sorted(FEED_COMPARATOR)
                .toList();

        boolean hasMore = items.size() > size;
        List<FeedItemResponse> pageItems = hasMore ? items.subList(0, size) : items;

        String nextCursor = null;
        if (hasMore && !pageItems.isEmpty()) {
            FeedItemResponse last = pageItems.getLast();
            nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
        }

        return FeedResponse.builder()
                .items(pageItems)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .size(pageItems.size())
                .build();
    }

    private String encodeCursor(LocalDateTime createdAt, Long id) {
        String raw = createdAt.toString() + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
    }

    private CursorData decodeCursor(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decoded.split("\\|");
            return new CursorData(
                    LocalDateTime.parse(parts[0]),
                    Long.parseLong(parts[1])
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor format");
        }
    }

    private record CursorData(LocalDateTime createdAt, Long id) {}
}
