package com.projetointegrador.natysync.trilha;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class QuizApiTest extends IntegracaoTest {

    private static final UUID ATIVIDADE_SEMEADA = UUID.fromString("00000000-0000-0000-0002-000000000001");

    @Test
    void quizTemQuatroPerguntasComQuatroAlternativas() {
        ResponseEntity<JsonNode> resposta = cliente()
                .get()
                .uri("/api/v1/atividades/" + ATIVIDADE_SEMEADA + "/quiz")
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("notaMinima").asInt()).isEqualTo(70);

        JsonNode perguntas = resposta.getBody().get("perguntas");
        assertThat(perguntas).hasSize(4);
        assertThat(perguntas.get(0).get("alternativas")).hasSize(4);
    }

    @Test
    void gabaritoNaoVazaNoPayload() {
        ResponseEntity<String> resposta = cliente()
                .get()
                .uri("/api/v1/atividades/" + ATIVIDADE_SEMEADA + "/quiz")
                .retrieve()
                .toEntity(String.class);

        assertThat(resposta.getBody()).doesNotContain("correta");
        assertThat(resposta.getBody()).doesNotContain("true");
    }

    @Test
    void atividadeSemQuizDevolveNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = cliente()
                .get()
                .uri("/api/v1/atividades/" + UUID.randomUUID() + "/quiz")
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void atividadeSemVideoNaoEErro() {
        ResponseEntity<JsonNode> resposta = cliente()
                .get()
                .uri("/api/v1/atividades/" + ATIVIDADE_SEMEADA)
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("imagemUrl").asText()).isNotBlank();
        assertThat(resposta.getBody().get("videoUrl").isNull()).isTrue();
        assertThat(resposta.getBody().get("possuiQuiz").asBoolean()).isTrue();
    }
}
