package com.pdgigs.application.service;

import com.pdgigs.domain.exception.UnauthorizedException;
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
        String normalizedEmail = email == null ? null : email.toLowerCase().trim();
        log.info("Authenticating user: {}", normalizedEmail);

        return userRepository.findByEmail(normalizedEmail)
                .switchIfEmpty(Mono.error(UnauthorizedException.invalidCredentials()))
                .flatMap(user -> passwordEncoder.matches(password, user.password())
                        .flatMap(matches -> {
                            if (!matches) {
                                return Mono.error(UnauthorizedException.invalidCredentials());
                            }
                            return jwtTokenProvider.generateToken(user);
                        })
                )
                .doOnSuccess(token -> log.info("User authenticated successfully: {}", normalizedEmail))
                .doOnError(error -> log.error("Authentication failed for: {}", normalizedEmail, error));
    }
}