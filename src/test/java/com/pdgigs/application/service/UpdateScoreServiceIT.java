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
import java.time.LocalDateTime;
import java.util.Date;
import java.io.InputStream;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestStubsConfig.class)
public class UpdateScoreServiceIT {

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
        InputStream is = UpdateScoreServiceIT.class.getClassLoader().getResourceAsStream("sample.pdf");
        if (is != null) {
            try (InputStream in = is) {
                pdfBytes = in.readAllBytes();
            }
        } else {
            pdfBytes = new byte[0];
        }
    }

    @BeforeEach
    void setup() {
        // ensure clean state
        reactiveMongoTemplate.dropCollection("scores").onErrorResume(e -> Mono.empty()).block();

        // Auth mocks (TestStubsConfig provides defaults but we reinforce here)
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(Mono.just(true));
        when(jwtTokenProvider.extractEmail(anyString())).thenReturn(Mono.just("test@example.com"));

        // user repo default
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

        // file storage default
        when(fileStoragePort.download(anyString())).thenAnswer(inv -> Mono.just((org.springframework.core.io.Resource) new org.springframework.core.io.ByteArrayResource(pdfBytes)));
    }

    private void insertScore(String id, String title, String author, String style) {
        Document doc = new Document("_id", id)
                .append("title", title)
                .append("author", author)
                .append("musicStyle", style)
                .append("filename", "stored-file.pdf")
                .append("createdAt", new Date());
        reactiveMongoTemplate.save(doc, "scores").block();
    }

    @Test
    void partialUpdate_singleField_updatesOnlyTitle() {
        // Given
        insertScore("S-1", "Melodía", "Bach", "Barroco");

        // When: PATCH only title
        webTestClient.patch()
                .uri("/api/scores/S-1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Concierto\"}")
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("S-1")
                .jsonPath("$.title").isEqualTo("Concierto")
                .jsonPath("$.author").isEqualTo("Bach")
                .jsonPath("$.musicStyle").isEqualTo("Barroco");

        // also assert persisted state
        Document saved = reactiveMongoTemplate.findById("S-1", Document.class, "scores").block();
        assert saved != null;
        assert "Concierto".equals(saved.getString("title"));
        assert "Bach".equals(saved.getString("author"));
        assert "Barroco".equals(saved.getString("musicStyle"));
    }

    @Test
    void partialUpdate_twoFields_updatesAuthorAndStyle_keepsTitle() {
        // Given
        insertScore("S-2", "Melodía", "Bach", "Barroco");

        // When: PATCH author and musicStyle
        webTestClient.patch()
                .uri("/api/scores/S-2")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("{\"author\":\"Mozart\",\"musicStyle\":\"Clásico\"}")
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("S-2")
                .jsonPath("$.title").isEqualTo("Melodía")
                .jsonPath("$.author").isEqualTo("Mozart")
                .jsonPath("$.musicStyle").isEqualTo("Clásico");

        Document saved = reactiveMongoTemplate.findById("S-2", Document.class, "scores").block();
        assert saved != null;
        assert "Melodía".equals(saved.getString("title"));
        assert "Mozart".equals(saved.getString("author"));
        assert "Clásico".equals(saved.getString("musicStyle"));
    }

    @Test
    void fullUpdate_allFields_areUpdated() {
        // Given
        insertScore("S-3", "Melodía", "Bach", "Barroco");

        // When: PATCH all fields
        webTestClient.patch()
                .uri("/api/scores/S-3")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Obertura\",\"author\":\"Haydn\",\"musicStyle\":\"Clásico\"}")
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("S-3")
                .jsonPath("$.title").isEqualTo("Obertura")
                .jsonPath("$.author").isEqualTo("Haydn")
                .jsonPath("$.musicStyle").isEqualTo("Clásico");

        Document saved = reactiveMongoTemplate.findById("S-3", Document.class, "scores").block();
        assert saved != null;
        assert "Obertura".equals(saved.getString("title"));
        assert "Haydn".equals(saved.getString("author"));
        assert "Clásico".equals(saved.getString("musicStyle"));
    }
}