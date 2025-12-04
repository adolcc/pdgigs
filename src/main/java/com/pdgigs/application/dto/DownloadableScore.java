package com.pdgigs.application.dto;

import org.springframework.core.io.Resource;
import java.util.Optional;

public record DownloadableScore(
        Resource resource,
        String filename,
        Optional<Long> contentLength
) {}