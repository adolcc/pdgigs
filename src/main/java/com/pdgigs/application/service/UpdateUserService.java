package com.pdgigs.application.service;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.UpdateUserUseCase;
import com.pdgigs.domain.port.output.PasswordEncoder;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.domain.exception.ConflictException;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.exception.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> updateName(String userId, String newName) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(userId)))
                .flatMap(u -> {
                    User updated = new User(
                            u.id(),
                            u.email(),
                            newName,
                            u.password(),
                            u.role(),
                            u.createdAt(),
                            LocalDateTime.now()
                    );
                    return userRepository.save(updated);
                });
    }

    @Override
    public Mono<User> updateEmail(String userId, String newEmail) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(userId)))
                .flatMap(u -> {
                    if (u.email().equalsIgnoreCase(newEmail)) {
                        return Mono.just(u); // nothing to change
                    }
                    return userRepository.existsByEmail(newEmail)
                            .flatMap(exists -> exists
                                    ? Mono.error(ConflictException.userAlreadyExists(newEmail))
                                    : userRepository.save(new User(
                                    u.id(),
                                    newEmail,
                                    u.name(),
                                    u.password(),
                                    u.role(),
                                    u.createdAt(),
                                    LocalDateTime.now()
                            )));
                });
    }

    @Override
    public Mono<User> changePassword(String userId, String currentPassword, String newPassword) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(userId)))
                .flatMap(u ->
                        passwordEncoder.matches(currentPassword, u.password())
                                .flatMap(matches -> {
                                    if (!matches) {
                                        return Mono.error(new ValidationException("currentPassword", "Current password is incorrect"));
                                    }
                                    return passwordEncoder.encode(newPassword)
                                            .flatMap(encoded -> userRepository.save(new User(
                                                    u.id(),
                                                    u.email(),
                                                    u.name(),
                                                    encoded,
                                                    u.role(),
                                                    u.createdAt(),
                                                    LocalDateTime.now()
                                            )));
                                })
                );
    }
}