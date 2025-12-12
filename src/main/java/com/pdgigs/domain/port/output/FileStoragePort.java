package com.pdgigs.domain.port.output;

import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileStoragePort {
    Mono<String> store(FilePart filePart, String filename);
    Mono<ReactiveGridFsResource> download(String storageId);
    Mono<Void> delete(String storageId);
}