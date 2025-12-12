package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.GetScoreMetadataUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateScoreRequest;
import com.pdgigs.domain.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class ScoreUpdateService {

    private final GetScoreMetadataUseCase getScoreMetadataUseCase;
    private final ScoreRepository scoreRepository;

    public Mono<Score> update(String id, UpdateScoreRequest req) {
        return getScoreMetadataUseCase.findById(id)
                .switchIfEmpty(Mono.empty())
                .flatMap(existing -> ReactiveSecurityContextHolder.getContext()
                        .flatMap(ctx -> Mono.justOrEmpty(ctx.getAuthentication()))
                        .flatMap(auth -> {
                            boolean isAdmin = auth.getAuthorities() != null &&
                                    auth.getAuthorities().stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .anyMatch(r -> "ROLE_ADMIN".equals(r));

                            String currentUser = auth.getName();
                            if (!isAdmin && existing.userEmail() != null && !existing.userEmail().equals(currentUser)) {
                                return Mono.error(ForbiddenException.forbidden("Not allowed to update this score"));
                            }

                            Score updated = new Score(
                                    existing.id(),
                                    req.title() != null ? req.title() : existing.title(),
                                    req.author() != null ? req.author() : existing.author(),
                                    req.musicStyle() != null ? req.musicStyle() : existing.musicStyle(),
                                    existing.filename(),
                                    existing.userEmail(),
                                    existing.createdAt()
                            );

                            return scoreRepository.save(updated);
                        })
                );
    }
}