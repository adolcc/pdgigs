package com.pdgigs.domain.port.input;

import com.pdgigs.domain.model.Score;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;


public interface UploadScoreUseCase {
    Mono<Score> upload(FilePart filePart,
                       String title,
                       String author,
                       String musicalStyle,
                       String userId);
}