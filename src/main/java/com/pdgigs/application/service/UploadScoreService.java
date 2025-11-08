package com.pdgigs.application.service;

import com.pdgigs.application.port.in.UploadScoreUseCase;
import com.pdgigs.domain.exception.FileSizeExceededException;
import com.pdgigs.domain.exception.InvalidFileFormatException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.out.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UploadScoreService implements UploadScoreUseCase {

    private final ScoreRepository scoreRepository;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final byte[] PDF_SIGNATURE = {0x25, 0x50, 0x44, 0x46}; // %PDF

    @Override
    public Mono<Score> uploadScore(byte[] pdfContent, String title, String author, String musicalStyle) {
        return Mono.just(pdfContent)
                .flatMap(content -> {
                    validateFileFormat(content);
                    validateFileSize(content);

                    Score score = Score.builder()
                            .title(title != null ? title : "")
                            .author(author != null ? author : "")
                            .musicalStyle(musicalStyle != null ? musicalStyle : "")
                            .pdfContent(content)
                            .fileSize((long) content.length)
                            .build();

                    return scoreRepository.save(score);
                });
    }

    private void validateFileFormat(byte[] content) {
        if (content == null || content.length < 4) {
            throw new InvalidFileFormatException("File format not allowed");
        }

        // Verificar que comience con %PDF
        for (int i = 0; i < PDF_SIGNATURE.length; i++) {
            if (content[i] != PDF_SIGNATURE[i]) {
                throw new InvalidFileFormatException("File format not allowed");
            }
        }
    }

    private void validateFileSize(byte[] content) {
        if (content.length > MAX_FILE_SIZE) {
            throw new FileSizeExceededException("The file exceeds the maximum allowed size");
        }
    }
}