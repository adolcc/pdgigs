package com.pdgigs.application.service;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.validator.ScoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteScoreService implements DeleteScoreUseCase {

    @Override
    public Mono<Void> deleteScore(String scoreId) {
        return null;
    }
}