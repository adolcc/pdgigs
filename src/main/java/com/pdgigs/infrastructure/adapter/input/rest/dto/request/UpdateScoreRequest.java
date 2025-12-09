package com.pdgigs.infrastructure.adapter.input.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScoreRequest {
    private String title;
    private String author;
    private String musicStyle;
}