package com.pdgigs.infrastructure.bootstrap;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.output.PasswordEncoder;
import com.pdgigs.domain.port.output.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.debug("AdminBootstrap: no admin credentials provided (APP_BOOTSTRAP_ADMIN_EMAIL/APP_BOOTSTRAP_ADMIN_PASSWORD). Skipping bootstrap.");
            return;
        }

        userRepository.findAll()
                .filter(u -> User.ROLE_ADMIN.equals(u.role()))
                .hasElements()
                .flatMap(hasAdmin -> {
                    if (hasAdmin) {
                        log.info("AdminBootstrap: admin user already exists, skipping bootstrap.");
                        return Mono.empty();
                    }
                    return userRepository.existsByEmail(adminEmail)
                            .flatMap(exists -> {
                                if (exists) {
                                    log.warn("AdminBootstrap: user with email {} already exists but no admin found. Not creating admin automatically.", adminEmail);
                                    return Mono.empty();
                                }
                                return passwordEncoder.encode(adminPassword)
                                        .flatMap(encoded -> {
                                            User admin = new User(
                                                    null,
                                                    adminEmail,
                                                    "admin",
                                                    encoded,
                                                    User.ROLE_ADMIN,
                                                    LocalDateTime.now(),
                                                    LocalDateTime.now()
                                            );
                                            return userRepository.save(admin)
                                                    .doOnNext(u -> log.info("AdminBootstrap: created initial admin user with email {}", u.email()));
                                        });
                            });
                })
                .doOnError(err -> log.error("AdminBootstrap: error while bootstrapping admin", err))
                .subscribe();
    }
}