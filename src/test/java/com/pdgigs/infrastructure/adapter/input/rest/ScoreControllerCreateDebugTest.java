package com.pdgigs.infrastructure.adapter.input.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ScoreControllerCreateDebugTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser(roles = "USER")
    public void debug_emptyTitle_shouldPrintResponse_authenticated() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ClassPathResource("test-score.pdf"));
        builder.part("title", "");
        builder.part("author", "John Doe");
        builder.part("musicalStyle", "Jazz");

        EntityExchangeResult<byte[]> result = webTestClient.post()
                .uri("/api/scores/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectBody(byte[].class)
                .returnResult();

        int rawStatus = result.getStatus().value();
        byte[] bodyBytes = result.getResponseBodyContent();
        String body = (bodyBytes == null || bodyBytes.length == 0) ? "" : new String(bodyBytes, StandardCharsets.UTF_8);

        System.out.println(">>> STATUS = " + rawStatus);
        System.out.println(">>> BODY = " + body);
    }
}