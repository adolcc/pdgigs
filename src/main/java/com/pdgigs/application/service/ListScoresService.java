package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.ListScoresUseCase;
import com.pdgigs.domain.port.output.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ListScoresService implements ListScoresUseCase {

    private final ScoreRepository scoreRepository;

    @Override
    public Flux<Score> listForAuthenticatedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMapMany(auth -> {
                    if (auth == null) {
                        return Flux.empty();
                    }

                    boolean isAdmin = auth.getAuthorities() != null && auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(role -> "ROLE_ADMIN".equals(role));

                    String currentUserEmail = auth.getName();

                    Flux<Score> allScores = scoreRepository.findAll();

                    if (isAdmin) {
                        return allScores;
                    }

                    return allScores.filter(s -> {
                        String uemail = s.userEmail();
                        return uemail != null && uemail.equals(currentUserEmail);
                    });
                });
    }
}