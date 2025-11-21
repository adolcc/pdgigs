package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.User;
import reactor.core.publisher.Mono;

public interface AdminChangeUserRoleUseCase {
    Mono<User> changeRole(String userId, String newRole);
}
