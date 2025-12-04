package com.pdgigs.application.service;

import com.pdgigs.application.dto.DownloadableScore;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.input.GetScorePdfUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScoreDownloadService {

    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final GetScorePdfUseCase getScorePdfUseCase;

    public Mono<DownloadableScore> prepareDownload(String id) {
        return getScoreMetadataUseCase.findById(id)
                .flatMap((Score metadata) ->
                        getScorePdfUseCase.getPdf(id)
                                .map(resource -> toDownloadableScore(id, metadata, resource))
                );
    }

    private DownloadableScore toDownloadableScore(String id, Score metadata, Resource resource) {
        String filename = resolveFilename(id, metadata);
        Optional<Long> lengthOpt = safeContentLength(resource);
        return new DownloadableScore(resource, filename, lengthOpt);
    }

    private String resolveFilename(String id, Score metadata) {
        return Optional.ofNullable(metadata)
                .map(Score::filename)
                .filter(name -> !name.isBlank())
                .orElse("score-" + id + ".pdf");
    }

    private Optional<Long> safeContentLength(Resource resource) {
        try {
            long length = resource.contentLength();
            return length >= 0 ? Optional.of(length) : Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}