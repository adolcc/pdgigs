package com.pdgigs.infrastructure.adapter.input.rest.mapper;

import com.pdgigs.domain.model.User;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserRestMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.id(),
                user.email(),
                user.name(),
                user.role()
        );
    }
}