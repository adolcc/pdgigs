package com.pdgigs.domain.port.output;

import jakarta.annotation.Resource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileStoragePort {
    Mono<String> store(FilePart filePart, String filename);
    Mono<Resource> download(String storageId);
}