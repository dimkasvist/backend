package ru.dimkasvist.dimkasvist.dto;

import java.util.List;

public record UsersSearchResponse(
    List<UserResponse> users,
    int currentPage,
    int totalPages,
    long totalItems
) {}
