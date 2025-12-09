package com.pdgigs.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.output.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EssentialUserFlowsIT {

    private static final String REGISTER_URI = "/auth/register";
    private static final String LOGIN_URI = "/auth/login";
    private static final String UPDATE_NAME_URI = "/users/me/name";
    private static final String UPDATE_EMAIL_URI = "/users/me/email";
    private static final String CHANGE_PW_URI = "/users/me/password";
    private static final String USER_BY_ID_URI = "/users/{id}";
    private static final String TEST_SECRET = "abcdefghijklmnopqrstuvwxyz0123456789!@";

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:6.0.8");

    static {
        MONGO.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("jwt.secret", () -> TEST_SECRET);
        registry.add("jwt.expiration-ms", () -> "3600000");
    }

    @LocalServerPort
    int port;

    @Autowired
    WebTestClient webClient;

    @Autowired
    UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanup() {
        userRepository.findAll()
                .flatMap(u -> userRepository.deleteById(u.id()))
                .then()
                .block();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String loginAndExtractToken(String email, String password) {
        final int maxAttempts = 5;
        final long sleepMs = 200L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            EntityExchangeResult<String> result = webClient.post()
                    .uri(LOGIN_URI)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("email", email, "password", password))
                    .exchange()
                    .expectBody(String.class)
                    .returnResult();

            String body = result.getResponseBody();
            int status = result.getStatus().value();

            if (status >= 200 && status < 300) {

                if (body == null || body.isBlank()) {

                    User stored = userRepository.findByEmail(email).block();
                    String storedHash = stored != null ? stored.password() : "null";
                    throw new AssertionError("Login returned empty body for '" + email + "'. storedPasswordHash=" + storedHash);
                }
                try {
                    Map<String, Object> map = objectMapper.readValue(body, new TypeReference<>() {});
                    if (map.containsKey("token") && map.get("token") != null) {
                        return map.get("token").toString();
                    } else {
                        User stored = userRepository.findByEmail(email).block();
                        String storedHash = stored != null ? stored.password() : "null";
                        throw new AssertionError("Login did not return token for '" + email + "'. responseBody=" + body +
                                " storedPasswordHash=" + storedHash);
                    }
                } catch (AssertionError ae) {
                    throw ae;
                } catch (Exception e) {
                    User stored = userRepository.findByEmail(email).block();
                    String storedHash = stored != null ? stored.password() : "null";
                    throw new RuntimeException("Failed to parse login body for '" + email + "'. body=" + body + " storedPasswordHash=" + storedHash, e);
                }
            }


            if (status == 401) {

                User stored = userRepository.findByEmail(email).block();
                if (stored == null) {

                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(sleepMs);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    } else {

                        var all = userRepository.findAll().collectList().block();
                        String dbSnapshot = (all == null || all.isEmpty()) ? "<empty>" :
                                all.stream()
                                        .map(usr -> String.format("{id=%s,email=%s}", usr.id(), usr.email()))
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("<none>");
                        throw new AssertionError("Login failed for '" + email + "' with 401 and user not found after retries. responseBody=" + body + " DB snapshot: " + dbSnapshot);
                    }
                } else {

                    String storedHash = stored.password();
                    throw new AssertionError("Login failed for '" + email + "'. responseBody=" + body + " storedPasswordHash=" + storedHash);
                }
            } else {

                User stored = userRepository.findByEmail(email).block();
                String storedHash = stored != null ? stored.password() : "null";
                var all = userRepository.findAll().collectList().block();
                String dbSnapshot = (all == null || all.isEmpty()) ? "<empty>" :
                        all.stream()
                                .map(usr -> String.format("{id=%s,email=%s}", usr.id(), usr.email()))
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("<none>");
                throw new AssertionError("Login returned status=" + status + " for '" + email + "'. responseBody=" + body +
                        " storedPasswordHash=" + storedHash + " DB snapshot: " + dbSnapshot);
            }
        }

        throw new AssertionError("Unreachable: loginAndExtractToken exhausted retries for " + email);
    }

    private User awaitUserByEmail(String email) {
        final int maxAttempts = 50;
        final long sleepMs = 200L;
        String emailLower = email.toLowerCase();
        for (int i = 0; i < maxAttempts; i++) {
            User u = userRepository.findByEmail(email).block();
            if (u != null) return u;
            if (!email.equals(emailLower)) {
                u = userRepository.findByEmail(emailLower).block();
                if (u != null) return u;
            }
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        var all = userRepository.findAll().collectList().block();
        String dbSnapshot = (all == null || all.isEmpty()) ? "<empty>" :
                all.stream()
                        .map(usr -> String.format("{id=%s,email=%s,name=%s,passwordHash=%s}", usr.id(), usr.email(), usr.name(), usr.password()))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("<none>");
        throw new AssertionError("User not found after register for " + email + ". DB snapshot: " + dbSnapshot);
    }

    private void awaitUserNotExists(String email) {
        final int maxAttempts = 50;
        final long sleepMs = 200L;
        String emailLower = email.toLowerCase();
        for (int i = 0; i < maxAttempts; i++) {
            User u = userRepository.findByEmail(email).block();
            User uLower = userRepository.findByEmail(emailLower).block();
            if (u == null && (email.equals(emailLower) || uLower == null)) {
                return;
            }
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        var all = userRepository.findAll().collectList().block();
        String dbSnapshot = (all == null || all.isEmpty()) ? "<empty>" :
                all.stream()
                        .map(usr -> String.format("{id=%s,email=%s,name=%s,passwordHash=%s}", usr.id(), usr.email(), usr.name(), usr.password()))
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("<none>");
        throw new AssertionError("User still present after waiting for deletion of " + email + ". DB snapshot: " + dbSnapshot);
    }

    @Test
    void profileUpdate_changes_token_and_login() {

        webClient.post()
                .uri(REGISTER_URI)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "profile-it@example.com", "name", "Profile IT", "password", "pass1234"))
                .exchange()
                .expectStatus().isCreated();


        String token = loginAndExtractToken("profile-it@example.com", "pass1234");

        webClient.put()
                .uri(UPDATE_NAME_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Profile IT Updated"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").exists();

        webClient.put()
                .uri(UPDATE_EMAIL_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "profile-it-new@example.com"))
                .exchange()
                .expectStatus().isOk();

        User byNew = awaitUserByEmail("profile-it-new@example.com");
        awaitUserNotExists("profile-it@example.com");
        User byOld = userRepository.findByEmail("profile-it@example.com").block();
        assertThat(byNew).as("user should exist with new email").isNotNull();
        assertThat(byOld).as("user should NOT exist with old email").isNull();

        webClient.post()
                .uri(LOGIN_URI)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "profile-it@example.com", "password", "pass1234"))
                .exchange()
                .expectStatus().isUnauthorized();

        String token2 = loginAndExtractToken("profile-it-new@example.com", "pass1234");
        Claims claims = parseToken(token2);
        assertThat(claims.getSubject()).isEqualTo("profile-it-new@example.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Profile IT Updated");
    }

    @Test
    void changePassword_invalidates_old_and_accepts_new() {
        webClient.post()
                .uri(REGISTER_URI)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "changepw-it@example.com", "name", "Change PW", "password", "initialPass!"))
                .exchange()
                .expectStatus().isCreated();

        User before = awaitUserByEmail("changepw-it@example.com");
        assertThat(before).isNotNull();
        String hashedBefore = before.password();
        assertThat(hashedBefore).isNotBlank();

        String token = loginAndExtractToken("changepw-it@example.com", "initialPass!");

        webClient.put()
                .uri(CHANGE_PW_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("currentPassword", "initialPass!", "newPassword", "newPass#2025"))
                .exchange()
                .expectStatus().isOk();

        User after = awaitUserByEmail("changepw-it@example.com");
        assertThat(after).isNotNull();
        String hashedAfter = after.password();
        assertThat(hashedAfter).isNotBlank();

        if (hashedBefore.equals(hashedAfter)) {
            fail("Password hash in DB did NOT change after calling change-password endpoint. " +
                    "hashedBefore=[" + hashedBefore + "] hashedAfter=[" + hashedAfter + "].");
        }

        String tokenNew = loginAndExtractToken("changepw-it@example.com", "newPass#2025");
        assertThat(tokenNew).isNotBlank();
    }

    @Test
    void unauthorized_modification_attempt_returns_4xx() {

        webClient.post().uri(REGISTER_URI)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "userA@example.com", "name", "User A", "password", "passwordA1!"))
                .exchange().expectStatus().isCreated();

        User storedA = awaitUserByEmail("userA@example.com");
        assertThat(storedA).isNotNull();

        String tokenA = loginAndExtractToken("userA@example.com", "passwordA1!");

        webClient.post().uri(REGISTER_URI)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "userB@example.com", "name", "User B", "password", "passwordB1!"))
                .exchange().expectStatus().isCreated();

        User userB = awaitUserByEmail("userB@example.com");
        String idB = userB != null ? userB.id() : "unknown-id";

        webClient.put()
                .uri(USER_BY_ID_URI, idB)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Hacked"))
                .exchange()
                .expectStatus().is4xxClientError();
    }
}