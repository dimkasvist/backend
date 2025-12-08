package ru.dimkasvist.dimkasvist.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeedResponse {

    /**
     * Список элементов ленты (фото или видео).
     */
    private List<FeedItemResponse> items;

    private String nextCursor;
    private boolean hasMore;
    private int size;
}
