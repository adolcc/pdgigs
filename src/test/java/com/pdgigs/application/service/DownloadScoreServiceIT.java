package com.pdgigs.application.service;

import com.pdgigs.domain.model.User;
import com.pdgigs.testconfig.TestStubsConfig;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.domain.port.output.PasswordEncoder;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestStubsConfig.class)
public class DownloadScoreServiceIT {

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
        InputStream is = DownloadScoreServiceIT.class.getClassLoader().getResourceAsStream("sample.pdf");
        if (is == null) throw new IOException("sample.pdf not found in classpath");
        try (InputStream in = is) {
            pdfBytes = in.readAllBytes();
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

        when(fileStoragePort.download(anyString()))
                .thenAnswer(invocation -> Mono.just((Resource) new ByteArrayResource(pdfBytes)));
    }

    @Test
    void given_score_exists_when_get_metadata_then_return_metadata() {
        Document doc = new Document("_id", "P-42")
                .append("title", "Symphony No.5")
                .append("author", "Ludwig van Beethoven")
                .append("musicStyle", "Classical")
                .append("filename", "stored-file.pdf")
                .append("createdAt", new Date());
        reactiveMongoTemplate.save(doc, "scores").block();

        webTestClient.get()
                .uri("/api/scores/P-42")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("P-42")
                .jsonPath("$.title").isEqualTo("Symphony No.5")
                .jsonPath("$.author").isEqualTo("Ludwig van Beethoven")
                .jsonPath("$.musicStyle").isEqualTo("Classical")
                .jsonPath("$.filename").isEqualTo("stored-file.pdf");
    }

    @Test
    void given_score_exists_when_download_pdf_then_return_pdf_bytes() {
        Document doc = new Document("_id", "P-42")
                .append("title", "Symphony No.5")
                .append("author", "Ludwig van Beethoven")
                .append("musicStyle", "Classical")
                .append("filename", "stored-file.pdf")
                .append("createdAt", new Date());
        reactiveMongoTemplate.save(doc, "scores").block();

        webTestClient.get()
                .uri("/api/scores/P-42/pdf")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody()
                .consumeWith(response -> {
                    byte[] body = response.getResponseBody();
                    assertThat(body).isEqualTo(pdfBytes);
                });
    }

    @Test
    void given_score_not_found_when_get_metadata_then_return_404() {
        webTestClient.get()
                .uri("/api/scores/P-99")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void given_score_not_found_when_download_pdf_then_return_404() {
        webTestClient.get()
                .uri("/api/scores/P-99/pdf")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_PDF)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").exists();
    }
}