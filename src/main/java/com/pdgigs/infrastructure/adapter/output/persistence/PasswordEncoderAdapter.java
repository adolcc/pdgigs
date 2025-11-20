package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.port.output.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Mono<String> encode(String rawPassword) {
        return Mono.fromCallable(() -> bCryptPasswordEncoder.encode(rawPassword));
    }

    @Override
    public Mono<Boolean> matches(String rawPassword, String encodedPassword) {
        return Mono.fromCallable(() -> bCryptPasswordEncoder.matches(rawPassword, encodedPassword));
    }
}