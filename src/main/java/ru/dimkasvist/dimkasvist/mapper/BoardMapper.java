package ru.dimkasvist.dimkasvist.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dimkasvist.dimkasvist.dto.BoardMediaResponse;
import ru.dimkasvist.dimkasvist.dto.BoardResponse;
import ru.dimkasvist.dimkasvist.dto.UserResponse;
import ru.dimkasvist.dimkasvist.entity.Board;
import ru.dimkasvist.dimkasvist.entity.BoardMedia;
import ru.dimkasvist.dimkasvist.entity.User;

@Component
@RequiredArgsConstructor
public class BoardMapper {

    private final MediaMapper mediaMapper;

    public BoardResponse toResponse(Board board, Long mediaCount, String coverImageUrl) {
        UserResponse userResponse = buildUser(board.getUser());

        return BoardResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .isPrivate(board.getIsPrivate())
                .user(userResponse)
                .mediaCount(mediaCount)
                .coverImageUrl(coverImageUrl)
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    public BoardMediaResponse toBoardMediaResponse(BoardMedia boardMedia) {
        return BoardMediaResponse.builder()
                .id(boardMedia.getId())
                .media(mediaMapper.toResponse(boardMedia.getMedia()))
                .addedAt(boardMedia.getAddedAt())
                .build();
    }

    private UserResponse buildUser(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
