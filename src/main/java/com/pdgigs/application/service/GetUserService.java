package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    @Override
    public Mono<User> getUserById(String userId) {
        log.info("Getting user by ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.userById(userId)))
                .doOnSuccess(user -> log.info("User found with ID: {}", userId))
                .doOnError(error -> log.error("Error getting user by ID: {}", userId, error));
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.user(email)))
                .doOnSuccess(user -> log.info("User found with email: {}", email))
                .doOnError(error -> log.error("Error getting user by email: {}", email, error));
    }
}