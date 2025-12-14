package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.*;
import ru.dimkasvist.dimkasvist.entity.Board;
import ru.dimkasvist.dimkasvist.entity.BoardMedia;
import ru.dimkasvist.dimkasvist.entity.Media;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.ForbiddenException;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.mapper.BoardMapper;
import ru.dimkasvist.dimkasvist.repository.BoardMediaRepository;
import ru.dimkasvist.dimkasvist.repository.BoardRepository;
import ru.dimkasvist.dimkasvist.repository.MediaRepository;
import ru.dimkasvist.dimkasvist.service.BoardService;
import ru.dimkasvist.dimkasvist.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardMediaRepository boardMediaRepository;
    private final MediaRepository mediaRepository;
    private final BoardMapper boardMapper;
    private final UserService userService;

    @Override
    @Transactional
    public BoardResponse createBoard(BoardRequest request) {
        User currentUser = userService.getCurrentUser();

        Board board = Board.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .user(currentUser)
                .build();

        Board saved = boardRepository.save(board);
        return boardMapper.toResponse(saved, 0L, null);
    }

    @Override
    @Transactional
    public BoardResponse updateBoard(Long id, BoardRequest request) {
        User currentUser = userService.getCurrentUser();
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        if (!board.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own boards");
        }

        if (request.getName() != null) {
            board.setName(request.getName());
        }
        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }
        if (request.getIsPrivate() != null) {
            board.setIsPrivate(request.getIsPrivate());
        }

        Board updated = boardRepository.save(board);
        long mediaCount = boardMediaRepository.countByBoardId(id);
        String coverUrl = getCoverImageUrl(id);
        
        return boardMapper.toResponse(updated, mediaCount, coverUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long id) {
        User currentUser = userService.getCurrentUser();
        Board board = boardRepository.findByIdAndAccessible(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found or not accessible with id: " + id));

        long mediaCount = boardMediaRepository.countByBoardId(id);
        String coverUrl = getCoverImageUrl(id);

        return boardMapper.toResponse(board, mediaCount, coverUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardsResponse getUserBoards(Long userId, int page, int size) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Board> boardsPage = boardRepository.findByUserId(userId, pageable);

        List<BoardResponse> boards = boardsPage.getContent().stream()
                .filter(board -> !board.getIsPrivate() || board.getUser().getId().equals(currentUser.getId()))
                .map(board -> {
                    long mediaCount = boardMediaRepository.countByBoardId(board.getId());
                    String coverUrl = getCoverImageUrl(board.getId());
                    return boardMapper.toResponse(board, mediaCount, coverUrl);
                })
                .collect(Collectors.toList());

        return BoardsResponse.builder()
                .boards(boards)
                .page(boardsPage.getNumber())
                .size(boardsPage.getSize())
                .totalElements(boardsPage.getTotalElements())
                .totalPages(boardsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardsResponse getMyBoards(int page, int size) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Board> boardsPage = boardRepository.findByUserId(currentUser.getId(), pageable);

        List<BoardResponse> boards = boardsPage.getContent().stream()
                .map(board -> {
                    long mediaCount = boardMediaRepository.countByBoardId(board.getId());
                    String coverUrl = getCoverImageUrl(board.getId());
                    return boardMapper.toResponse(board, mediaCount, coverUrl);
                })
                .collect(Collectors.toList());

        return BoardsResponse.builder()
                .boards(boards)
                .page(boardsPage.getNumber())
                .size(boardsPage.getSize())
                .totalElements(boardsPage.getTotalElements())
                .totalPages(boardsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void deleteBoard(Long id) {
        User currentUser = userService.getCurrentUser();
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        if (!board.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own boards");
        }

        boardRepository.delete(board);
    }

    @Override
    @Transactional
    public BoardMediaResponse addMediaToBoard(Long boardId, Long mediaId) {
        User currentUser = userService.getCurrentUser();
        
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + boardId));

        if (!board.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only add media to your own boards");
        }

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

        if (boardMediaRepository.existsByBoardIdAndMediaId(boardId, mediaId)) {
            throw new IllegalStateException("Media already exists in this board");
        }

        BoardMedia boardMedia = BoardMedia.builder()
                .board(board)
                .media(media)
                .build();

        BoardMedia saved = boardMediaRepository.save(boardMedia);
        return boardMapper.toBoardMediaResponse(saved);
    }

    @Override
    @Transactional
    public void removeMediaFromBoard(Long boardId, Long mediaId) {
        User currentUser = userService.getCurrentUser();
        
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + boardId));

        if (!board.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only remove media from your own boards");
        }

        BoardMedia boardMedia = boardMediaRepository.findByBoardIdAndMediaId(boardId, mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found in this board"));

        boardMediaRepository.delete(boardMedia);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardMediaListResponse getBoardMedia(Long boardId, int page, int size) {
        User currentUser = userService.getCurrentUser();
        
        Board board = boardRepository.findByIdAndAccessible(boardId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found or not accessible with id: " + boardId));

        Pageable pageable = PageRequest.of(page, size);
        Page<BoardMedia> mediaPage = boardMediaRepository.findByBoardId(boardId, pageable);

        List<BoardMediaResponse> items = mediaPage.getContent().stream()
                .map(boardMapper::toBoardMediaResponse)
                .collect(Collectors.toList());

        return BoardMediaListResponse.builder()
                .items(items)
                .page(mediaPage.getNumber())
                .size(mediaPage.getSize())
                .totalElements(mediaPage.getTotalElements())
                .totalPages(mediaPage.getTotalPages())
                .build();
    }

    private String getCoverImageUrl(Long boardId) {
        Pageable pageable = PageRequest.of(0, 1);
        Page<BoardMedia> firstMedia = boardMediaRepository.findByBoardId(boardId, pageable);
        
        if (firstMedia.hasContent()) {
            return firstMedia.getContent().get(0).getMedia().getFilePath();
        }
        
        return null;
    }
}
