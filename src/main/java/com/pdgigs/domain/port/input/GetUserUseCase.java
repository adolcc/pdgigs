package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.User;
import reactor.core.publisher.Mono;

public interface GetUserUseCase {
    Mono<User> getUserById(String userId);
    Mono<User> getUserByEmail(String email);
}