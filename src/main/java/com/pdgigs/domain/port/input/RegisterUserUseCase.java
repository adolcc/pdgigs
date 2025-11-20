package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.User;
import reactor.core.publisher.Mono;

public interface RegisterUserUseCase {
    Mono<User> registerUser(String email, String name, String password);
}