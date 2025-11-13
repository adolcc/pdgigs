package com.pdgigs.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UploadScoreRequest(

        @NotNull(message = "PDF file is required")
        MultipartFile file,

        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 255, message = "Author must not exceed 255 characters")
        String author,

        @Size(max = 100, message = "Musical style must not exceed 100 characters")
        String musicalStyle
) { }