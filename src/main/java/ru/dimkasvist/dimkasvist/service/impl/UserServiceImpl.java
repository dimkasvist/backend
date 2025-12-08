package ru.dimkasvist.dimkasvist.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dimkasvist.dimkasvist.dto.UserResponse;
import ru.dimkasvist.dimkasvist.entity.User;
import ru.dimkasvist.dimkasvist.exception.ResourceNotFoundException;
import ru.dimkasvist.dimkasvist.repository.UserRepository;
import ru.dimkasvist.dimkasvist.security.GoogleUserPrincipal;
import ru.dimkasvist.dimkasvist.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User getOrCreateUser(String googleId, String email, String displayName, String avatarUrl) {
        return userRepository.findByGoogleId(googleId)
                .map(user -> {
                    user.setEmail(email);
                    user.setDisplayName(displayName);
                    user.setAvatarUrl(avatarUrl);
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .googleId(googleId)
                            .email(email)
                            .displayName(displayName)
                            .avatarUrl(avatarUrl)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @Override
    @Transactional
    public User getCurrentUser() {
        GoogleUserPrincipal principal = (GoogleUserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        return getOrCreateUser(
                principal.googleId(),
                principal.email(),
                principal.name(),
                principal.pictureUrl()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return UserResponse.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
