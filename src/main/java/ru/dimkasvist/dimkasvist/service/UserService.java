package ru.dimkasvist.dimkasvist.service;

import ru.dimkasvist.dimkasvist.dto.UserResponse;
import ru.dimkasvist.dimkasvist.entity.User;

public interface UserService {

    User getOrCreateUser(String keycloakId, String email, String displayName, String avatarUrl);

    User getCurrentUser();

    UserResponse getUserById(Long id);
}
