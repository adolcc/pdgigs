package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.infrastructure.adapter.output.persistence.entity.UserDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MongoUserRepository extends ReactiveMongoRepository <UserDocument, String>{
    Mono<UserDocument> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}
