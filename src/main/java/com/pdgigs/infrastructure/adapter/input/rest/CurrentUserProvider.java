package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.input.GetUserUseCase;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CurrentUserProvider {

    private final GetUserUseCase getUserUseCase;

    public CurrentUserProvider(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }

    public Mono<String> currentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null) return Mono.empty();
                    String email = auth.getName();
                    if (email == null || email.isBlank()) return Mono.empty();
                    return getUserUseCase.getUserByEmail(email).map(User::id);
                });
    }
}