package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.User;
import reactor.core.publisher.Mono;

public interface UpdateUserUseCase {
    Mono<User> updateName(String userId, String newName);
    Mono<User> updateEmail(String userId, String newEmail);
    Mono<User> changePassword(String userId, String currentPassword, String newPassword);
}