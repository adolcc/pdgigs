package com.pdgigs.application.service;

import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.output.ScoreRepository;
import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ScoreSecurityService {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    public Mono<Boolean> hasAccessToScore(String scoreId, String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .flatMap(currentUser -> {
                    if (User.ROLE_ADMIN.equals(currentUser.role())) {
                        return Mono.just(true);
                    }

                    return scoreRepository.findById(scoreId)
                            .map(score -> isScoreOwner(score, currentUser))
                            .defaultIfEmpty(false);
                })
                .defaultIfEmpty(false);
    }

    private boolean isScoreOwner(Score score, User currentUser) {
        return (score.userId() != null && score.userId().equals(currentUser.id())) ||
                (score.userEmail() != null && score.userEmail().equals(currentUser.email()));
    }
}