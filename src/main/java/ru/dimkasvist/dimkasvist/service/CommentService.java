package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.CommentRequest;
import ru.dimkasvist.dimkasvist.dto.CommentResponse;
import ru.dimkasvist.dimkasvist.dto.CommentsResponse;

public interface CommentService {

    CommentResponse addComment(Long mediaId, CommentRequest request);

    CommentsResponse getComments(Long mediaId, String cursor, int size);

    CommentResponse updateComment(Long commentId, CommentRequest request);

    void deleteComment(Long commentId);
}
