package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.User;
import reactor.core.publisher.Flux;

public interface AdminListUsersUseCase {
    Flux<User> listAllUsers();
}