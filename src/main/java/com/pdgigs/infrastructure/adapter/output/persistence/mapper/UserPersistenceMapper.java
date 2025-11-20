package com.pdgigs.infrastructure.adapter.output.persistence.mapper;

import com.pdgigs.domain.model.User;
import com.pdgigs.infrastructure.adapter.output.persistence.entity.UserDocument;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserDocument toDocument(User user) {
        return new UserDocument(
                user.id(),
                user.email(),
                user.name(),
                user.password(),
                user.role(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    public User toDomain(UserDocument document) {
        return new User(
                document.getId(),
                document.getEmail(),
                document.getName(),
                document.getPassword(),
                document.getRole(),
                document.getCreateAT(),
                document.getUpdateAT()
        );
    }
}