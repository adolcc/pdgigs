package com.pdgigs.infrastructure.adapter.output.persistence.entity;

import com.pdgigs.domain.model.Annotation;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "annotations")
public class AnnotationDocument {
    @Id
    private String id;
    private String scoreId;
    private Integer pageNumber;
    private String annotationsJson;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public static AnnotationDocument fromDomain(Annotation a) {
        AnnotationDocument d = new AnnotationDocument();
        d.setId(a.id());
        d.setScoreId(a.scoreId());
        d.setPageNumber(a.pageNumber());
        d.setAnnotationsJson(a.annotationsJson());
        d.setUpdatedAt(a.updatedAt());
        d.setUpdatedBy(a.updatedBy());
        return d;
    }

    public Annotation toDomain() {
        return new Annotation(id, scoreId, pageNumber, annotationsJson, updatedBy);
    }
}