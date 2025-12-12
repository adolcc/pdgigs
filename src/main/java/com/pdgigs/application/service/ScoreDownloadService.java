package com.pdgigs.application.service;

import com.pdgigs.application.dto.DownloadableScore;
import com.pdgigs.domain.exception.ForbiddenException;
import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.output.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.InputStream;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreDownloadService {

    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final FileStoragePort fileStoragePort;

    public Mono<DownloadableScore> prepareDownload(String scoreId) {
        log.debug("prepareDownload called for id={}", scoreId);

        return getScoreMetadataUseCase.findById(scoreId)
                .switchIfEmpty(Mono.error(ResourceNotFoundException.score(scoreId)))
                .flatMap(metadata ->
                        ReactiveSecurityContextHolder.getContext()
                                .flatMap(ctx -> Mono.justOrEmpty(ctx.getAuthentication()))
                                .flatMap(auth -> {
                                    boolean isAdmin = auth.getAuthorities() != null && auth.getAuthorities().stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .anyMatch(r -> "ROLE_ADMIN".equals(r));

                                    String currentUserEmail = auth.getName();

                                    if (!isAdmin) {
                                        String ownerEmail = metadata.userEmail();
                                        if (ownerEmail == null || !ownerEmail.equals(currentUserEmail)) {
                                            log.warn("User {} attempted to access score {} owned by {}", currentUserEmail, scoreId, ownerEmail);
                                            return Mono.error(ForbiddenException.forbidden("Not allowed to download this score"));
                                        }
                                    }

                                    log.debug("Authorized to download id={}, loading resource identifier={}", scoreId, metadata.filename());

                                    return fileStoragePort.download(metadata.filename())
                                            .switchIfEmpty(Mono.error(ResourceNotFoundException.score(scoreId)))
                                            .flatMap(reactiveResource ->
                                                    reactiveResource.getInputStream()
                                                            .subscribeOn(Schedulers.boundedElastic())
                                                            .map((InputStream is) -> {
                                                                InputStreamResource isr = new InputStreamResource(is);
                                                                return new DownloadableScore(isr, metadata.filename(), Optional.empty());
                                                            })
                                            )
                                            .doOnError(e -> log.error("Error loading resource for {}: {}", scoreId, e.getMessage(), e));
                                })
                                .switchIfEmpty(Mono.error(ForbiddenException.forbidden("Not authorized")))
                );
    }
}