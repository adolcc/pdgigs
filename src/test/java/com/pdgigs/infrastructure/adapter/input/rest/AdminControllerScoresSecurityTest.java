package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.config.TestSecurityConfig;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.*;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import com.pdgigs.infrastructure.adapter.input.rest.dto.response.ScoreResponse;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.UserRestMapper;
import com.pdgigs.infrastructure.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(AdminController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AdminController - Scores Management Tests")
class AdminControllerScoresSecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean private AdminListScoresUseCase listScoresUseCase;
    @MockitoBean private AdminDeleteScoreUseCase deleteScoreUseCase;
    @MockitoBean private AdminDeleteUserUseCase deleteUserUseCase;
    @MockitoBean private AdminChangeUserRoleUseCase changeRoleUseCase;
    @MockitoBean private AdminListUsersUseCase listUsersUseCase;
    @MockitoBean private GetUserUseCase getUserUseCase;
    @MockitoBean private ScoreRestMapper scoreRestMapper;
    @MockitoBean private UserRestMapper userRestMapper;

    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private com.pdgigs.domain.port.output.UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Configura el filter mock para que devuelva un Mono<Void> válido
        when(jwtAuthenticationFilter.filter(any(ServerWebExchange.class), any(WebFilterChain.class)))
                .thenAnswer(invocation -> {
                    WebFilterChain chain = invocation.getArgument(1);
                    ServerWebExchange exchange = invocation.getArgument(0);
                    return chain.filter(exchange); // Simula el comportamiento real
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should list all scores and return 200")
    void listAllScores_AdminUser_Returns200() {
        // Given
        Score score1 = new Score("score-1", "Title 1", "Author 1", "Style 1", new byte[]{}, 1024L, "user-1", "user1@test.com", null);
        Score score2 = new Score("score-2", "Title 2", "Author 2", "Style 2", new byte[]{}, 2048L, "user-2", "user2@test.com", null);

        ScoreResponse response1 = new ScoreResponse("score-1", "Title 1", "Author 1", "Style 1", 1024L, null);
        ScoreResponse response2 = new ScoreResponse("score-2", "Title 2", "Author 2", "Style 2", 2048L, null);

        when(listScoresUseCase.listAllScores()).thenReturn(Flux.just(score1, score2));
        when(scoreRestMapper.toResponse(score1)).thenReturn(response1);
        when(scoreRestMapper.toResponse(score2)).thenReturn(response2);

        // When & Then
        webTestClient.get()
                .uri("/admin/scores")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("score-1")
                .jsonPath("$[1].id").isEqualTo("score-2");

        verify(listScoresUseCase).listAllScores();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete score and return 204")
    void deleteScore_AdminUser_Returns204() {
        // Given
        String scoreId = "507f1f77bcf86cd799439011";
        when(deleteScoreUseCase.deleteScore(eq(scoreId))).thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/admin/scores/{id}", scoreId)
                .exchange()
                .expectStatus().isNoContent();

        verify(deleteScoreUseCase).deleteScore(scoreId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 when non-admin tries to access scores")
    void listAllScores_NonAdmin_Returns403() {
        webTestClient.get()
                .uri("/admin/scores")
                .exchange()
                .expectStatus().isForbidden();
    }
}