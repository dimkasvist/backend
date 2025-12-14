package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.*;

public interface BoardService {
    BoardResponse createBoard(BoardRequest request);
    BoardResponse updateBoard(Long id, BoardRequest request);
    BoardResponse getBoard(Long id);
    BoardsResponse getUserBoards(Long userId, int page, int size);
    BoardsResponse getMyBoards(int page, int size);
    void deleteBoard(Long id);
    BoardMediaResponse addMediaToBoard(Long boardId, Long mediaId);
    void removeMediaFromBoard(Long boardId, Long mediaId);
    BoardMediaListResponse getBoardMedia(Long boardId, int page, int size);
}
