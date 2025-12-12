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

        Flux<?> usersFlux = Optional.ofNullable(userRepository.findAll()).orElse(Flux.empty());

        usersFlux
                .filter(user -> true)
                .flatMap(user -> {
                    return Flux.empty();
                })
                .subscribe();
    }
}