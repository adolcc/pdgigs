package com.pdgigs.application.service;

import com.pdgigs.domain.model.User;
import com.pdgigs.testconfig.TestStubsConfig;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.domain.port.output.PasswordEncoder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestStubsConfig.class)
public class UploadScoreServiceIT {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStoragePort fileStoragePort;

    private static byte[] pdfBytes;

    @BeforeAll
    static void loadPdf() throws IOException {
        ClassPathResource resource = new ClassPathResource("sample.pdf");
        try (InputStream is = resource.getInputStream()) {
            pdfBytes = is.readAllBytes();
        }
    }

    @BeforeEach
    void setup() {

        reactiveMongoTemplate.dropCollection("scores").onErrorResume(e -> Mono.empty()).block();

        when(jwtTokenProvider.validateToken(anyString())).thenReturn(Mono.just(true));
        when(jwtTokenProvider.extractEmail(anyString())).thenReturn(Mono.just("test@example.com"));

        User testUser = new User(
                "user-id",
                "test@example.com",
                "Test User",
                "irrelevant-password",
                "ROLE_USER",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(testUser));

        when(fileStoragePort.store(any(org.springframework.http.codec.multipart.FilePart.class), anyString()))
                .thenReturn(Mono.just("stored-file.pdf"));
    }

    @Test
    void given_non_pdf_file_when_upload_then_return_file_format_not_allowed() {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        parts.add("file", new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return "not-a-pdf.txt";
            }
        });

        webTestClient.post()
                .uri("/api/scores/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("File format not allowed");
    }

    @Test
    void given_pdf_exceeding_size_limit_when_upload_then_return_file_too_large() {

        byte[] largePdf = new byte[11 * 1024 * 1024];

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new ByteArrayResource(largePdf) {
            @Override
            public String getFilename() {
                return "huge.pdf";
            }
        });

        webTestClient.post()
                .uri("/api/scores/upload")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(parts))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("The file exceeds the maximum allowed size");
    }
}