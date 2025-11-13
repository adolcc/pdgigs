package com.pdgigs.application.service;

import com.pdgigs.application.port.input.UpdateScoreUseCase;
import com.pdgigs.domain.exception.ScoreNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UpdateScoreService implements UpdateScoreUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Mono<Score> updateMetadata(String id, String title, String author, String musicalStyle) {
        return scoreRepository.findById(id)
                .switchIfEmpty(Mono.error(new ScoreNotFoundException("Score not found: " + id)))
                .flatMap(existing -> {

                    String newTitle = title != null ? title : existing.title();
                    String newAuthor = author != null ? author : existing.author();
                    String newMusicalStyle = musicalStyle != null ? musicalStyle : existing.musicalStyle();

                    Score updated = new Score(
                            existing.id(),
                            newTitle,
                            newAuthor,
                            newMusicalStyle,
                            existing.pdfContent(),
                            existing.fileSize()
                    );

                    return scoreRepository.save(updated);
                });
    }
}