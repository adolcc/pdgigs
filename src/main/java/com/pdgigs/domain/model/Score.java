package com.pdgigs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Score {
    private String id;
    private String title;
    private String author;
    private String musicalStyle;
    private byte[] pdfContent;
    private Long fileSize;
}