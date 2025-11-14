package com.pdgigs.infrastructure.adapter.input.rest;

import com.pdgigs.domain.port.input.UpdateScoreUseCase;
import com.pdgigs.infrastructure.adapter.input.rest.dto.request.UpdateScoreRequest;
import com.pdgigs.infrastructure.adapter.input.rest.mapper.ScoreRestMapper;
import com.pdgigs.infrastructure.config.SecurityConfig;
import com.pdgigs.domain.model.Score;
import com.pdgigs.infrastructure.adapter.input.rest.helper.ScoreMockFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(ScoreControllerUpdate.class)
@Import({ScoreRestMapper.class, SecurityConfig.class})
@DisplayName("Controller: actualización de metadata")
class ScoreControllerUpdateTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UpdateScoreUseCase updateScoreUseCase;

    @Test
    @DisplayName("PATCH /api/scores/{id} - Actualización parcial (título)")
    void patch_UpdateTitleOnly() {
        Score updated = ScoreMockFactory.create("1", "Concierto", "Bach", "Barroco");

        when(updateScoreUseCase.updateMetadata("1", "Concierto", null, null))
                .thenReturn(Mono.just(updated));

        UpdateScoreRequest req = new UpdateScoreRequest("Concierto", null, null);

        webTestClient.patch()
                .uri("/api/scores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.title").isEqualTo("Concierto")
                .jsonPath("$.author").isEqualTo("Bach")
                .jsonPath("$.musicalStyle").isEqualTo("Barroco");
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Actualización múltiple (autor y estilo)")
    void patch_UpdateAuthorAndStyle() {
        Score updated = ScoreMockFactory.create("1", "Melodía", "Mozart", "Clásico");

        when(updateScoreUseCase.updateMetadata("1", null, "Mozart", "Clásico"))
                .thenReturn(Mono.just(updated));

        UpdateScoreRequest req = new UpdateScoreRequest(null, "Mozart", "Clásico");

        webTestClient.patch()
                .uri("/api/scores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.title").isEqualTo("Melodía")
                .jsonPath("$.author").isEqualTo("Mozart")
                .jsonPath("$.musicalStyle").isEqualTo("Clásico");
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Actualización completa")
    void patch_UpdateAll() {
        Score updated = ScoreMockFactory.create("1", "Nueva", "NuevoAutor", "NuevoEstilo");

        when(updateScoreUseCase.updateMetadata("1", "Nueva", "NuevoAutor", "NuevoEstilo"))
                .thenReturn(Mono.just(updated));

        UpdateScoreRequest req = new UpdateScoreRequest("Nueva", "NuevoAutor", "NuevoEstilo");

        webTestClient.patch()
                .uri("/api/scores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Nueva")
                .jsonPath("$.author").isEqualTo("NuevoAutor")
                .jsonPath("$.musicalStyle").isEqualTo("NuevoEstilo");
    }

    @Test
    @DisplayName("PATCH /api/scores/{id} - Not found -> 404")
    void patch_NotFound() {
        when(updateScoreUseCase.updateMetadata("missing", null, null, null))
                .thenReturn(Mono.error(new com.pdgigs.domain.exception.ScoreNotFoundException("missing")));

        UpdateScoreRequest req = new UpdateScoreRequest(null, null, null);

        webTestClient.patch()
                .uri("/api/scores/missing")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }
}