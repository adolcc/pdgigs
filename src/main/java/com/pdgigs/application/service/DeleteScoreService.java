package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteScoreService implements DeleteScoreUseCase {

    private final ScoreRepository scoreRepository;
    private final FileStoragePort fileStoragePort;

    @Override
    public Mono<Void> deleteById(String id) {
        return scoreRepository.findById(id)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.score(id)))
                .flatMap((Score existing) ->
                        fileStoragePort.delete(existing.filename())
                                .then(scoreRepository.deleteById(id))
                );
    }
}