package com.pdgigs.infrastructure.adapter.output.storage;

import com.pdgigs.domain.port.output.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GridFsFileStorageAdapter implements FileStoragePort {

    private final ReactiveGridFsTemplate gridFsTemplate;

    @Override
    public Mono<String> store(FilePart filePart, String filename) {
        return gridFsTemplate.store(filePart.content(), filename)
                .map(Object::toString);
    }

    @Override
    public Mono<ReactiveGridFsResource> download(String storageId) {
        try {
            ObjectId objectId = new ObjectId(storageId);
            return gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)))
                    .flatMap(gridFsTemplate::getResource);
        } catch (IllegalArgumentException ex) {
            return gridFsTemplate.findOne(Query.query(Criteria.where("filename").is(storageId)))
                    .flatMap(gridFsTemplate::getResource);
        }
    }

    @Override
    public Mono<Void> delete(String storageId) {
        try {
            ObjectId objectId = new ObjectId(storageId);
            return gridFsTemplate.delete(Query.query(Criteria.where("_id").is(objectId)));
        } catch (IllegalArgumentException ex) {
            return gridFsTemplate.delete(Query.query(Criteria.where("filename").is(storageId)));
        }
    }
}