package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.DeleteScoreUseCase;
import com.pdgigs.domain.exception.ScoreNotFoundException;
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

import static org.mockito.Mockito.when;

@WebFluxTest(ScoreController.class)
@Import({ScoreRestMapper.class, SecurityConfig.class})
@DisplayName("Controller: Eliminación de partituras")
class ScoreControllerDeleteTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DeleteScoreUseCase deleteScoreUseCase;

    @Test
    @DisplayName("DELETE /api/scores/{id} - Debe eliminar partitura exitosamente")
    void deleteScore_WhenScoreExists_ShouldReturn204() {
        // GIVEN
        String scoreId = "P-55";
        when(deleteScoreUseCase.deleteScore(scoreId)).thenReturn(Mono.empty());

        // WHEN & THEN
        webTestClient.delete()
                .uri("/api/scores/{id}", scoreId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/scores/{id} - Debe retornar 404 cuando la partitura no existe")
    void deleteScore_WhenScoreNotFound_ShouldReturn404() {
        // GIVEN
        String scoreId = "P-99";
        when(deleteScoreUseCase.deleteScore(scoreId))
                .thenReturn(Mono.error(new ScoreNotFoundException("Score with ID P-99 not found.")));

        // WHEN & THEN
        webTestClient.delete()
                .uri("/api/scores/{id}", scoreId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Score with ID P-99 not found.")
                .jsonPath("$.status").isEqualTo(404);
    }
}