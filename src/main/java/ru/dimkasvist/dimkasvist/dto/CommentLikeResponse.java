package ru.dimkasvist.dimkasvist.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentLikeResponse {

    private boolean liked;
    private long likesCount;
}
