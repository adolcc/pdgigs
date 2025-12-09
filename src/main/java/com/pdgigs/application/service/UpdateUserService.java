package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ConflictException;
import com.pdgigs.domain.exception.ForbiddenException;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.exception.UnauthorizedException;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.UpdateUserUseCase;
import com.pdgigs.domain.port.output.PasswordEncoder;
import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> updateName(String userId, String newName) {
        return updateNameAs(userId, newName, userId);
    }

    @Override
    public Mono<User> updateEmail(String userId, String newEmail) {
        return updateEmailAs(userId, newEmail, userId);
    }

    @Override
    public Mono<User> changePassword(String userId, String currentPassword, String newPassword) {
        return changePasswordAs(userId, currentPassword, newPassword, userId);
    }

    @Override
    public Mono<User> updateProfile(String userId, String newName, String newEmail) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(userId)))
                .flatMap(existing -> {
                    String updatedName = (newName == null || newName.isBlank()) ? existing.name() : newName;
                    String updatedEmail = (newEmail == null || newEmail.isBlank())
                            ? existing.email()
                            : newEmail.toLowerCase().trim();

                    boolean emailChanged = !existing.email().equalsIgnoreCase(updatedEmail);

                    Mono<Void> uniquenessCheck = Mono.empty();
                    if (emailChanged) {
                        uniquenessCheck = userRepository.existsByEmail(updatedEmail)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(ConflictException.userAlreadyExists(updatedEmail));
                                    }
                                    return Mono.empty();
                                });
                    }

                    return uniquenessCheck.then(Mono.defer(() -> {
                        User updated = new User(
                                existing.id(),
                                updatedEmail,
                                updatedName,
                                existing.password(),
                                existing.role(),
                                existing.createdAt(),
                                LocalDateTime.now()
                        );
                        return userRepository.save(updated);
                    }));
                });
    }

    @Override
    public Mono<User> updateNameAs(String targetUserId, String newName, String callerUserId) {
        return authorizeAndFind(targetUserId, callerUserId)
                .flatMap(target -> {
                    User updated = new User(
                            target.id(),
                            target.email(),
                            newName,
                            target.password(),
                            target.role(),
                            target.createdAt(),
                            LocalDateTime.now()
                    );
                    return userRepository.save(updated);
                });
    }

    @Override
    public Mono<User> updateEmailAs(String targetUserId, String newEmail, String callerUserId) {
        return authorizeAndFind(targetUserId, callerUserId)
                .flatMap(target -> {
                    User updated = new User(
                            target.id(),
                            newEmail.toLowerCase().trim(),
                            target.name(),
                            target.password(),
                            target.role(),
                            target.createdAt(),
                            LocalDateTime.now()
                    );
                    return userRepository.save(updated);
                });
    }

    @Override
    public Mono<User> changePasswordAs(String targetUserId, String currentPassword, String newPassword, String callerUserId) {
        return authorizeAndFind(targetUserId, callerUserId)
                .flatMap(target ->
                        passwordEncoder.matches(currentPassword, target.password())
                                .flatMap(matches -> {
                                    if (!matches) {
                                        return Mono.error(UnauthorizedException.invalidCredentials());
                                    }
                                    return passwordEncoder.encode(newPassword)
                                            .flatMap(hashedNew -> {
                                                User updated = new User(
                                                        target.id(),
                                                        target.email(),
                                                        target.name(),
                                                        hashedNew,
                                                        target.role(),
                                                        target.createdAt(),
                                                        LocalDateTime.now()
                                                );
                                                return userRepository.save(updated);
                                            });
                                })
                );
    }

    private Mono<User> authorizeAndFind(String targetUserId, String callerUserId) {
        return userRepository.findById(targetUserId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(targetUserId)))
                .flatMap(target -> {
                    if (target.id().equals(callerUserId)) {
                        return Mono.just(target);
                    }

                    return userRepository.findById(callerUserId)
                            .flatMap(caller -> {
                                if (caller.role() != null && caller.role().equals(User.ROLE_ADMIN)) {
                                    return Mono.just(target);
                                }
                                return Mono.error(ForbiddenException.forbidden("user " + callerUserId + " cannot modify " + targetUserId));
                            });
                });
    }
}