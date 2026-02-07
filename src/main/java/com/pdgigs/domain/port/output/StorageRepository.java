package com.pdgigs.domain.port.output;

import reactor.core.publisher.Mono;

public interface StorageRepository {
    /**
     * Genera una URL temporal para que el frontend suba el archivo directamente.
     */
    Mono<String> generatePresignedUploadUrl(String fileName);
}