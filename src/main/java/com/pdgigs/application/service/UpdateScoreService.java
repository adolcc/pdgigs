package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateScoreService implements UpdateScoreUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<Score> updateMetadata(String scoreId, String title, String author, String musicalStyle) {
        return scoreRepository.findById(scoreId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.score(scoreId)))
                .flatMap(existing -> {
                    String newTitle = firstNonBlank(title, existing.title());
                    String newAuthor = firstNonBlank(author, existing.author());
                    String newMusicStyle = firstNonBlank(musicalStyle, existing.musicStyle());

                    Score updated = new Score(
                            existing.id(),
                            newTitle,
                            newAuthor,
                            newMusicStyle,
                            existing.filename(),
                            existing.createdAt() == null ? LocalDateTime.now() : existing.createdAt()
                    );

                    return scoreRepository.save(updated);
                });
    }

    private String firstNonBlank(String candidate, String fallback) {
        if (candidate == null) return fallback;
        if (candidate.isBlank()) return fallback;
        return candidate;
    }
}