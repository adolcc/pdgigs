package com.pdgigs.infrastructure.adapter.output.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scores")
public class ScoreDocument {
    @Id
    private String id;

    private String title;
    private String author;
    private String musicStyle;
    private String filename;
    private String userEmail;
    private LocalDateTime createdAt;

    public static ScoreDocument fromDomain(com.pdgigs.domain.model.Score s) {
        if (s == null) return null;
        return new ScoreDocument(
                s.id(),
                s.title(),
                s.author(),
                s.musicStyle(),
                s.filename(),
                s.userEmail(),
                s.createdAt()
        );
    }

    public com.pdgigs.domain.model.Score toDomain() {
        return new com.pdgigs.domain.model.Score(
                id,
                title,
                author,
                musicStyle,
                filename,
                userEmail,
                createdAt
        );
    }
}