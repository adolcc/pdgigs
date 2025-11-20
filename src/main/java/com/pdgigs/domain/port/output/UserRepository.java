package com.pdgigs.domain.port.output;

import com.pdgigs.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);
    Mono<User> findByEmail(String email);
    Mono<User> findById(String id);
    Mono<Boolean> existsByEmail(String email);
    Mono<Void> deleteById(String id);
}