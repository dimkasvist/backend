package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.LikeResponse;
import ru.dimkasvist.dimkasvist.dto.UserLikesResponse;

public interface LikeService {

    LikeResponse toggleLike(Long mediaId);

    LikeResponse getLikeStatus(Long mediaId);

    UserLikesResponse getUserLikes(Long userId, String cursor, int size);
}
