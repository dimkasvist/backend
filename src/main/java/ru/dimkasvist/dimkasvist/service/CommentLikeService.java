package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.CommentLikeResponse;

import java.util.Collection;
import java.util.Map;

public interface CommentLikeService {

    CommentLikeResponse toggleLike(Long commentId);

    CommentLikeResponse getLikeStatus(Long commentId);

    Map<Long, CommentLikeResponse> getLikeInfo(Collection<Long> commentIds);
}
