package ru.dimkasvist.dimkasvist.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dimkasvist.dimkasvist.dto.*;
import ru.dimkasvist.dimkasvist.service.BoardService;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<@NonNull BoardResponse> createBoard(@RequestBody BoardRequest request) {
        BoardResponse response = boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull BoardResponse> updateBoard(
            @PathVariable Long id,
            @RequestBody BoardRequest request
    ) {
        BoardResponse response = boardService.updateBoard(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull BoardResponse> getBoard(@PathVariable Long id) {
        BoardResponse response = boardService.getBoard(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<@NonNull BoardsResponse> getMyBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        BoardsResponse response = boardService.getMyBoards(page, Math.min(size, 50));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<@NonNull BoardsResponse> getUserBoards(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        BoardsResponse response = boardService.getUserBoards(userId, page, Math.min(size, 50));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NonNull Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{boardId}/media/{mediaId}")
    public ResponseEntity<@NonNull BoardMediaResponse> addMediaToBoard(
            @PathVariable Long boardId,
            @PathVariable Long mediaId
    ) {
        BoardMediaResponse response = boardService.addMediaToBoard(boardId, mediaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{boardId}/media/{mediaId}")
    public ResponseEntity<@NonNull Void> removeMediaFromBoard(
            @PathVariable Long boardId,
            @PathVariable Long mediaId
    ) {
        boardService.removeMediaFromBoard(boardId, mediaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{boardId}/media")
    public ResponseEntity<@NonNull BoardMediaListResponse> getBoardMedia(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        BoardMediaListResponse response = boardService.getBoardMedia(boardId, page, Math.min(size, 50));
        return ResponseEntity.ok(response);
    }
}
