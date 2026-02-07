package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ConflictException;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.RegisterUserUseCase;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.PasswordEncoder;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.domain.validator.UserValidator;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<User> registerUser(String email, String name, String password) {
        log.info("Registering user with email: {}", email);

        return UserValidator.validateCredentials(email, password)
                .then(checkEmailNotExists(email))
                .then(passwordEncoder.encode(password))
                .flatMap(hashedPassword -> {
                    User newUser = new User(
                            null,
                            email.toLowerCase().trim(),
                            name,
                            hashedPassword,
                            User.ROLE_USER,
                            LocalDateTime.now(),
                            null
                    );
                    return userRepository.save(newUser);
                })
                .doOnSuccess(user -> log.info("User registered successfully with ID: {}", user.id()))
                .doOnError(error -> log.error("Error registering user: {}", error.getMessage()));
    }

    private Mono<Void> checkEmailNotExists(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(ConflictException.userAlreadyExists(email));
                    }
                    return Mono.empty();
                });
    }

    public Mono<AuthResponse> registerUserAndGenerateToken(String email, String name, String password) {
        return registerUser(email, name, password)
                .flatMap(user ->
                        jwtTokenProvider.generateToken(user)
                                .map(token -> AuthResponse.fromTokenAndUser(token, user))
                )
                .doOnError(e -> log.error("Error registering+tokenizing user {}: {}", email, e.getMessage()));
    }

    public Mono<User> findByEmail(String email) {
        if (email == null) return Mono.empty();
        return userRepository.findByEmail(email.toLowerCase().trim());
    }
}