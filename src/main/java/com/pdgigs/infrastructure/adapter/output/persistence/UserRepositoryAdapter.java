package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.infrastructure.adapter.output.persistence.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final MongoUserRepository mongoRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Mono<User> save(User user) {
        return Mono.just(user)
                .map(mapper::toDocument)
                .flatMap(mongoRepository::save)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return mongoRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<User> findById(String id) {
        return mongoRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return mongoRepository.existsByEmail(email);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }

    @Override
    public Flux<User> findAll() {
        return mongoRepository.findAll()
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(User user) {
        if (user == null || user.id() == null) {
            return Mono.empty();
        }
        return deleteById(user.id());
    }

    @Override
    public Mono<Void> deleteAll() {
        return mongoRepository.deleteAll();
    }
}