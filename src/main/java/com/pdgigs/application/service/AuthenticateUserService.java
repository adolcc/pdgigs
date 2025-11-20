package com.pdgigs.application.service;

import com.pdgigs.domain.exception.validation.UserValidationError;
import com.pdgigs.domain.port.input.AuthenticateUserUseCase;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.PasswordEncoder;
import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<String> authenticate(String email, String password) {
        log.info("Authenticating user: {}", email);

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserValidationError.InvalidCredentials().toException()))
                .flatMap(user -> passwordEncoder.matches(password, user.password())
                        .flatMap(matches -> {
                            if (!matches) {
                                return Mono.error(new UserValidationError.InvalidCredentials().toException());
                            }
                            return jwtTokenProvider.generateToken(user);
                        })
                )
                .doOnSuccess(token -> log.info("User authenticated successfully: {}", email))
                .doOnError(error -> log.error("Authentication failed for: {}", email, error));
    }
}