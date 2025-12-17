package ru.dimkasvist.dimkasvist.security;

import java.security.Principal;

public record GoogleUserPrincipal(
        String googleId,
        String email,
        String name,
        String pictureUrl
) implements Principal {
    
    @Override
    public String getName() {
        return googleId;
    }
}
