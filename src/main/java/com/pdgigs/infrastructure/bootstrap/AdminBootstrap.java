package com.pdgigs.infrastructure.bootstrap;

import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // null-safe: si userRepository.findAll() devolviera null, usamos Flux.empty()
        Flux<?> usersFlux = Optional.ofNullable(userRepository.findAll()).orElse(Flux.empty());

        usersFlux
                .filter(user -> /* tu filtro */ true)
                .flatMap(user -> {
                    // lógica de bootstrap (crear admin si no existe, etc.)
                    return Flux.empty();
                })
                .subscribe();
    }
}