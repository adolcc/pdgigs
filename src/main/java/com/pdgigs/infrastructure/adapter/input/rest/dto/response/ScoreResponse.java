package com.pdgigs.infrastructure.adapter.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreResponse {
    private String id;
    private String title;
    private String author;
    private String musicalStyle;
    private Long fileSize;
}