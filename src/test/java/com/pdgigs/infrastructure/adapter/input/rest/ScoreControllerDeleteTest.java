package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.DomainExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.GlobalFallbackHandler;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ScoreControllerDelete.class)
@Import({
        ScoreRestMapper.class,
        SecurityConfig.class,
        DomainExceptionHandler.class,
        GlobalFallbackHandler.class
})
@DisplayName("Controller: Eliminación de partituras")
class ScoreControllerDeleteTest {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";
    private static final String NON_EXISTENT_ID = "507f1f77bcf86cd799439099";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DeleteScoreUseCase deleteScoreUseCase;

    @Test
    @DisplayName("DELETE /api/scores/{id} - Partitura existe → 204")
    void deleteScore_ScoreExists_Returns204() {
        // GIVEN
        when(deleteScoreUseCase.deleteScore(SCORE_ID))
                .thenReturn(Mono.empty());

        // WHEN & THEN
        webTestClient.delete()
                .uri("/api/scores/{id}", SCORE_ID)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(deleteScoreUseCase).deleteScore(SCORE_ID);
    }

    @Test
    @DisplayName("DELETE /api/scores/{id} - Partitura no existe → 404")
    void deleteScore_ScoreNotFound_Returns404() {
        // GIVEN
        when(deleteScoreUseCase.deleteScore(NON_EXISTENT_ID))
                .thenReturn(Mono.error(ResourceNotFoundException.score(NON_EXISTENT_ID)));

        // WHEN & THEN
        webTestClient.delete()
                .uri("/api/scores/{id}", NON_EXISTENT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").value(msg ->
                        msg.toString().contains("Score not found with ID: " + NON_EXISTENT_ID))
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.errorCode").isEqualTo("RESOURCE_NOT_FOUND");

        verify(deleteScoreUseCase).deleteScore(NON_EXISTENT_ID);
    }

    @Test
    @DisplayName("DELETE /api/scores/{id} - Error interno → 500")
    void deleteScore_InternalError_Returns500() {
        // GIVEN
        when(deleteScoreUseCase.deleteScore(SCORE_ID))
                .thenReturn(Mono.error(new RuntimeException("Database connection lost")));

        // WHEN & THEN
        webTestClient.delete()
                .uri("/api/scores/{id}", SCORE_ID)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(deleteScoreUseCase).deleteScore(SCORE_ID);
    }
}