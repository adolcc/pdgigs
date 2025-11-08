package com.pdgigs.infrastructure.adapter.output.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scores")
public class ScoreEntity {
    @Id
    private String id;
    private String title;
    private String author;
    private String musicalStyle;
    private byte[] pdfContent;
    private Long fileSize;
}