package com.pdgigs.application.service;

import com.pdgigs.domain.model.Annotation;
import com.pdgigs.domain.port.output.AnnotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnnotationService {

    private final AnnotationRepository repository;

    public Mono<Annotation> saveAnnotations(String scoreId, Integer pageNumber, String annotationsJson) {
        return resolveCurrentUserEmail()
                .defaultIfEmpty("")
                .flatMap(userEmail -> {
                    Annotation toSave = new Annotation(null, scoreId, pageNumber, annotationsJson, userEmail);
                    return repository.save(toSave);
                });
    }

    public Mono<Annotation> loadAnnotations(String scoreId, Integer pageNumber) {
        return repository.findByScoreIdAndPageNumber(scoreId, pageNumber);
    }

    private Mono<String> resolveCurrentUserEmail() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    if (auth == null) return Mono.just("");
                    Object principal = auth.getPrincipal();
                    if (principal instanceof UserDetails) {
                        return Mono.just(((UserDetails) principal).getUsername());
                    }
                    String name = auth.getName();
                    return Mono.just(name != null ? name : "");
                })
                .onErrorResume(e -> Mono.just(""));
    }
}