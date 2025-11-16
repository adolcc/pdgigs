package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.exception.ResourceNotFoundException;
import com.pdgigs.domain.exception.validation.ScoreValidationError;
import com.pdgigs.domain.model.Score;
import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateScoreRequest;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.DomainExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.GlobalFallbackHandler;
import com.pdgigs.infrastructure.adapter.input.rest.exception.handler.ValidationExceptionHandler;
import com.pdgigs.infrastructure.adapter.input.rest.helper.ScoreMockFactory;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ScoreControllerUpdate.class)
@Import({
        ScoreRestMapper.class,
        SecurityConfig.class,
        DomainExceptionHandler.class,
        ValidationExceptionHandler.class,
        GlobalFallbackHandler.class
})
@DisplayName("Controller: Actualización de metadata")
class ScoreControllerUpdateTest {

    private static final String SCORE_ID = "507f1f77bcf86cd799439011";
    private static final String NON_EXISTENT_ID = "507f1f77bcf86cd799439099";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UpdateScoreUseCase updateScoreUseCase;

    @Test
    @DisplayName("PATCH /api/scores/{id} - Solo título → 200")
    void updateScore_TitleOnly_Returns200() {
        // GIVEN
        Score updated = ScoreMockFactory.create(SCORE_ID, "Concierto de Brandenburgo", "Bach", "Barroco");

        when(updateScoreUseCase.updateMetadata(SCORE_ID, "Concierto de Brandenburgo", null, null))
                .thenReturn(Mono.just(updated));

        UpdateScoreRequest req = new UpdateScoreRequest("Concierto de Brandenburgo", null, null);

        // WHEN & THEN
        webTestClient.patch()
                .uri("/api/scores/{id}", SCORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(SCORE_ID)
                .jsonPath("$.title").isEqualTo("Concierto de Brandenburgo")
                .jsonPath("$.author").isEqualTo("Bach")
                .jsonPath("$.musicalStyle").isEqualTo("Barroco");

        verify(updateScoreUseCase).updateMetadata(SCORE_ID, "Concierto de Brandenburgo", null, null);
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Autor y estilo → 200")
    void updateScore_AuthorAndStyle_Returns200() {
        // GIVEN
        Score updated = ScoreMockFactory.create(SCORE_ID, "Melodía", "Mozart", "Clásico");

        when(updateScoreUseCase.updateMetadata(SCORE_ID, null, "Mozart", "Clásico"))
                .thenReturn(Mono.just(updated));

        UpdateScoreRequest req = new UpdateScoreRequest(null, "Mozart", "Clásico");

        // WHEN & THEN
        webTestClient.patch()
                .uri("/api/scores/{id}", SCORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(SCORE_ID)
                .jsonPath("$.title").isEqualTo("Melodía")
                .jsonPath("$.author").isEqualTo("Mozart")
                .jsonPath("$.musicalStyle").isEqualTo("Clásico");
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Todos los campos → 200")
    void updateScore_AllFields_Returns200() {
        // GIVEN
        Score updated = ScoreMockFactory.create(SCORE_ID, "Sinfonía No. 40", "Mozart", "Clásico");

        when(updateScoreUseCase.updateMetadata(SCORE_ID, "Sinfonía No. 40", "Mozart", "Clásico"))
                .thenReturn(Mono.just(updated));

        UpdateScoreRequest req = new UpdateScoreRequest("Sinfonía No. 40", "Mozart", "Clásico");

        // WHEN & THEN
        webTestClient.patch()
                .uri("/api/scores/{id}", SCORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Sinfonía No. 40")
                .jsonPath("$.author").isEqualTo("Mozart")
                .jsonPath("$.musicalStyle").isEqualTo("Clásico");
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Partitura no existe → 404")
    void updateScore_ScoreNotFound_Returns404() {
        // GIVEN
        when(updateScoreUseCase.updateMetadata(NON_EXISTENT_ID, "Nuevo Título", null, null))
                .thenReturn(Mono.error(ResourceNotFoundException.score(NON_EXISTENT_ID)));

        UpdateScoreRequest req = new UpdateScoreRequest("Nuevo Título", null, null);

        // WHEN & THEN
        webTestClient.patch()
                .uri("/api/scores/{id}", NON_EXISTENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.errorCode").isEqualTo("RESOURCE_NOT_FOUND")
                .jsonPath("$.message").value(msg ->
                        msg.toString().contains("Score not found with ID: " + NON_EXISTENT_ID));

        verify(updateScoreUseCase).updateMetadata(NON_EXISTENT_ID, "Nuevo Título", null, null);
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Título en blanco → 400")
    void updateScore_BlankTitle_Returns400() {
        // GIVEN
        when(updateScoreUseCase.updateMetadata(SCORE_ID, "   ", null, null))
                .thenReturn(Mono.error(new ScoreValidationError.TitleCannotBeBlank().toException()));

        UpdateScoreRequest req = new UpdateScoreRequest("   ", null, null);

        // WHEN & THEN
        webTestClient.patch()
                .uri("/api/scores/{id}", SCORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Sin cambios → 200")
    void updateScore_NoChanges_Returns200() {
        // GIVEN
        Score unchanged = ScoreMockFactory.create(SCORE_ID, "Original", "Autor", "Estilo");

        when(updateScoreUseCase.updateMetadata(SCORE_ID, null, null, null))
                .thenReturn(Mono.just(unchanged));

        UpdateScoreRequest req = new UpdateScoreRequest(null, null, null);

        // WHEN & THEN
        webTestClient.patch()
                .uri("/api/scores/{id}", SCORE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(SCORE_ID);
    }
}