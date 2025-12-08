package ru.dimkasvist.dimkasvist.security;

public record GoogleUserPrincipal(
        String googleId,
        String email,
        String name,
        String pictureUrl
) {}
