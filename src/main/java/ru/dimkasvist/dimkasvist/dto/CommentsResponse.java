package ru.dimkasvist.dimkasvist.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommentsResponse {

    private List<CommentResponse> comments;
    private String nextCursor;
    private boolean hasMore;
    private long totalCount;
}
