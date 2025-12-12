package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.UploadScoreUseCase;
import com.pdgigs.domain.port.input.GetUserUseCase;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UploadScoreService implements UploadScoreUseCase {

    private static final Logger log = LoggerFactory.getLogger(UploadScoreService.class);

    private final FileStoragePort fileStoragePort;
    private final ScoreRepository scoreRepository;
    private final GetUserUseCase getUserUseCase;

    @Override
    public Mono<Score> upload(FilePart filePart, String title, String author, String musicStyle, String userId) {
        if (filePart == null) {
            return Mono.error(new IllegalArgumentException("File is required"));
        }

        final String originalFileName = filePart.filename();
        if (originalFileName == null || originalFileName.isBlank()) {
            log.warn("Upload attempted with empty filename");
        } else {
            String lower = originalFileName.toLowerCase();
            if (!lower.endsWith(".pdf")) {
                log.warn("Uploading non-pdf file: {}", originalFileName);
            }
        }

        Mono<String> emailMono;
        if (userId != null && !userId.isBlank()) {

            emailMono = getUserUseCase.getUserById(userId)
                    .map(User::email)
                    .defaultIfEmpty("")
                    .onErrorResume(ex -> {

                        log.info("getUserById({}) did not resolve to a user; treating provided value as email", userId);
                        return Mono.just(userId);
                    });
        } else {
            emailMono = ReactiveSecurityContextHolder.getContext()
                    .map(ctx -> ctx.getAuthentication())
                    .map((Authentication auth) -> {
                        Object principal = auth.getPrincipal();
                        if (principal instanceof UserDetails) {
                            return ((UserDetails) principal).getUsername();
                        }
                        return auth.getName();
                    })
                    .defaultIfEmpty("")
                    .onErrorResume(e -> {
                        log.warn("Unable to resolve authenticated user; continuing without email");
                        return Mono.just("");
                    });
        }

        log.info("Starting upload flow for file='{}'", originalFileName);

        return fileStoragePort.store(filePart, originalFileName)

                .doOnNext(storageId -> log.info("File stored successfully"))
                .flatMap(storageId ->
                        emailMono.flatMap(emailValue -> {
                            String userEmail = (emailValue == null || emailValue.isBlank()) ? null : emailValue;
                            Score toSave = new Score(null, title, author, musicStyle, storageId, userEmail);
                            log.debug("Persisting Score entity (non-sensitive info only)");
                            return scoreRepository.save(toSave)
                                    .doOnSuccess(saved -> log.info("Score saved successfully"))
                                    .doOnError(e -> log.error("Error saving score entity", e));
                        })
                )
                .doOnError(e -> log.error("Upload flow failed", e));
    }
}