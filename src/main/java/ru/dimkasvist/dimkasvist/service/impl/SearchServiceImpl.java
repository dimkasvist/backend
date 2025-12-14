package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.FeedItemResponse;
import ru.dimkasvist.dimkasvist.dto.SearchResponse;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.mapper.MediaMapper;
import ru.dimkasvist.dimkasvist.repository.MediaRepository;
import ru.dimkasvist.dimkasvist.service.SearchService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(String query, int size) {
        if (query == null || query.isBlank()) {
            return SearchResponse.builder()
                    .items(List.of())
                    .totalResults(0)
                    .query("")
                    .build();
        }

        String trimmedQuery = query.trim();
        int pageSize = Math.max(1, Math.min(size, 100));
        PageRequest pageRequest = PageRequest.of(0, pageSize);

        List<Media> mediaList = mediaRepository.searchMedia(trimmedQuery, pageRequest);

        List<FeedItemResponse> items = mediaList.stream()
                .map(mediaMapper::toFeedItem)
                .toList();

        return SearchResponse.builder()
                .items(items)
                .totalResults(items.size())
                .query(trimmedQuery)
                .build();
    }
}
