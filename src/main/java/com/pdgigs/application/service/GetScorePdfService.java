package com.pdgigs.application.service;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetScorePdfService implements GetScorePdfUseCase {

    private final ScoreRepository scoreRepository;
    private final FileStoragePort fileStoragePort;

    @Override
    public Mono<Resource> getPdf(String scoreId) {
        return scoreRepository.findById(scoreId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.score(scoreId)))
                .flatMap((Score s) -> {
                    String storageId = s.filename();
                    if (storageId == null || storageId.isBlank()) {
                        return Mono.error(ResourceNotFoundException.score(scoreId));
                    }
                    return fileStoragePort.download(storageId)
                            .cast(Resource.class)
                            .switchIfEmpty(Mono.error(ResourceNotFoundException.score(scoreId)));
                });
    }
}