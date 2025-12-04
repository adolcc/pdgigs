package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ValidationException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.UploadScoreUseCase;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.ScoreRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class UploadScoreService implements UploadScoreUseCase {

    private static final long MAX_BYTES = 10L * 1024L * 1024L;

    private final ScoreRepository scoreRepository;
    private final FileStoragePort fileStorage;

    public UploadScoreService(ScoreRepository scoreRepository, FileStoragePort fileStorage) {
        this.scoreRepository = scoreRepository;
        this.fileStorage = fileStorage;
    }

    @Override
    public Mono<Score> upload(FilePart filePart, String title, String author, String musicalStyle, String userId) {
        String filename = filePart == null ? null : filePart.filename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return Mono.error(ValidationException.invalidField("file", "File format not allowed"));
        }

        HttpHeaders headers = filePart.headers();
        long contentLength = headers != null ? headers.getContentLength() : -1L;
        if (contentLength > MAX_BYTES) {
            return Mono.error(ValidationException.invalidField("file", "The file exceeds the maximum allowed size"));
        }

        String safeTitle = title == null ? "" : title;
        String safeAuthor = author == null ? "" : author;
        String safeStyle = musicalStyle == null ? "" : musicalStyle;

        return Objects.requireNonNull(fileStorage)
                .store(filePart, filename)
                .flatMap(storedRef -> {
                    Score score = new Score(
                            null,
                            safeTitle,
                            safeAuthor,
                            safeStyle,
                            storedRef,
                            LocalDateTime.now()
                    );
                    return scoreRepository.save(score);
                });
    }
}