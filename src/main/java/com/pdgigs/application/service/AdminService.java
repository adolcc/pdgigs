package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ValidationException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.AdminChangeUserRoleUseCase;
import com.pdgigs.domain.port.input.AdminDeleteScoreUseCase;
import com.pdgigs.domain.port.input.AdminDeleteUserUseCase;
import com.pdgigs.domain.port.input.AdminListScoresUseCase;
import com.pdgigs.domain.port.input.AdminListUsersUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService implements
        AdminListScoresUseCase,
        AdminDeleteScoreUseCase,
        AdminDeleteUserUseCase,
        AdminChangeUserRoleUseCase,
        AdminListUsersUseCase {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    @Override
    public Flux<Score> listAllScores() {
        return scoreRepository.findAll();
    }

    @Override
    public Mono<Void> deleteScore(String scoreId) {
        return scoreRepository.findById(scoreId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.score(scoreId)))
                .flatMap(s -> scoreRepository.deleteById(scoreId));
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(userId)))
                .flatMap(u -> {
                    if (User.ROLE_ADMIN.equals(u.role())) {
                        return userRepository.findAll()
                                .filter(x -> User.ROLE_ADMIN.equals(x.role()))
                                .count()
                                .flatMap(count -> {
                                    if (count <= 1) {
                                        return Mono.error(ValidationException.invalidField("user", "Cannot delete the last admin"));
                                    }
                                    return userRepository.deleteById(userId);
                                });
                    }
                    return userRepository.deleteById(userId);
                });
    }

    @Override
    public Mono<User> changeRole(String userId, String newRole) {
        if (!User.ROLE_ADMIN.equals(newRole) && !User.ROLE_USER.equals(newRole)) {
            return Mono.error(ValidationException.invalidField("role", "Invalid role. Use ROLE_USER or ROLE_ADMIN"));
        }

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(userId)))
                .flatMap(u -> {
                    if (User.ROLE_ADMIN.equals(u.role()) && User.ROLE_USER.equals(newRole)) {
                        return userRepository.findAll()
                                .filter(x -> User.ROLE_ADMIN.equals(x.role()))
                                .count()
                                .flatMap(count -> {
                                    if (count <= 1) {
                                        return Mono.error(ValidationException.invalidField("role", "Cannot demote the last admin"));
                                    }
                                    User updated = new User(
                                            u.id(),
                                            u.email(),
                                            u.name(),
                                            u.password(),
                                            newRole,
                                            u.createdAt(),
                                            LocalDateTime.now()
                                    );
                                    return userRepository.save(updated);
                                });
                    }

                    User updated = new User(
                            u.id(),
                            u.email(),
                            u.name(),
                            u.password(),
                            newRole,
                            u.createdAt(),
                            LocalDateTime.now()
                    );
                    return userRepository.save(updated);
                });
    }

    @Override
    public Flux<User> listAllUsers() {
        return userRepository.findAll();
    }
}