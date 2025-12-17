package ru.dimkasvist.dimkasvist.service;

import org.springframework.data.domain.Pageable;
import ru.dimkasvist.dimkasvist.dto.UserResponse;
import ru.dimkasvist.dimkasvist.dto.UsersSearchResponse;
import ru.dimkasvist.dimkasvist.entity.User;

public interface UserService {

    User getOrCreateUser(String keycloakId, String email, String displayName, String avatarUrl);

    User getCurrentUser();

    UserResponse getUserById(Long id);

    UsersSearchResponse searchUsers(String query, Pageable pageable);
}
