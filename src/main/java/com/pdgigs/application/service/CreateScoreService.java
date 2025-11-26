package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.CreateScoreUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.domain.validator.FileValidator;
import com.pdgigs.domain.validator.ScoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateScoreService implements CreateScoreUseCase {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    @Override
    public Mono<Score> createScore(byte[] pdfContent, String title, String author, String musicalStyle, String userEmail) {
        log.debug("Creating new score: {} by {} for user: {}", title, author, userEmail);

        return userRepository.findByEmail(userEmail)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + userEmail)))
                .flatMap(user ->
                        FileValidator.validateFile(pdfContent)
                                .then(ScoreValidator.validateMetadata(title, author, musicalStyle))
                                .then(Mono.fromCallable(() -> new Score(
                                        null,
                                        title,
                                        author,
                                        musicalStyle,
                                        pdfContent,
                                        (long) pdfContent.length,
                                        user.id(),      // ✅ Guardar ID del usuario
                                        user.email(),   // ✅ Guardar email del usuario
                                        LocalDateTime.now()
                                )))
                                .flatMap(scoreRepository::save)
                )
                .doOnSuccess(score -> log.info("Score created successfully with ID: {} for user: {}", score.id(), userEmail))
                .doOnError(error -> log.error("Error creating score: {} for user: {}", title, userEmail, error));
    }
}