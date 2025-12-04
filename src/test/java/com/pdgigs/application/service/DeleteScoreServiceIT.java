package com.pdgigs.application.service;

import com.pdgigs.domain.model.User;
import com.pdgigs.testconfig.TestStubsConfig;
import com.pdgigs.domain.port.output.FileStoragePort;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.domain.port.output.UserRepository;
import com.pdgigs.domain.port.output.PasswordEncoder;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
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
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestStubsConfig.class)
public class DeleteScoreServiceIT {

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

    @BeforeAll
    static void loadSample() throws IOException {
        InputStream is = DeleteScoreServiceIT.class.getClassLoader().getResourceAsStream("sample.pdf");

        if (is != null) is.close();
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

        when(fileStoragePort.delete(anyString())).thenReturn(Mono.empty());
    }

    private void insertScore(String id, String title, String author, String style, String filename) {
        Document doc = new Document("_id", id)
                .append("title", title)
                .append("author", author)
                .append("musicStyle", style)
                .append("filename", filename)
                .append("createdAt", new Date());
        reactiveMongoTemplate.save(doc, "scores").block();
    }

    @Test
    void delete_existing_score_should_remove_entity_and_file_and_return_204() {
        insertScore("P-55", "Alguna", "Autor", "Estilo", "stored-file.pdf");

        when(fileStoragePort.delete("stored-file.pdf")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/scores/P-55")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNoContent();

        Document found = reactiveMongoTemplate.findById("P-55", Document.class, "scores").block();
        assert found == null;

        verify(fileStoragePort, times(1)).delete("stored-file.pdf");
    }

    @Test
    void delete_nonexistent_score_should_return_404() {
        webTestClient.delete()
                .uri("/api/scores/P-99")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNotFound();
    }
}