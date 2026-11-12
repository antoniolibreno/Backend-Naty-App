package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class QuizConteudoApiTest extends BaseDeQuizApiTest {

    @BeforeEach
    void preparar() {
        prepararAtividade();
    }

    private ResponseEntity<JsonNode> criar(Map<String, Object> corpo) {
        return comoNaty(HttpMethod.POST, "/api/v1/quizzes", corpo);
    }

    @Test
    @SuppressWarnings("unchecked")
    void criaQuizComPerguntasEAlternativasENotaPadrao() {
        ResponseEntity<JsonNode> resposta =
                criar(corpoDoQuiz(atividadeId, null, pergunta("P1", 3, 0), pergunta("P2", 2, 1)));

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        JsonNode quiz = resposta.getBody();
        assertThat(quiz.get("notaMinima").asInt()).isEqualTo(70);
        assertThat(quiz.get("atividadeId").asText()).isEqualTo(atividadeId.toString());
        assertThat(quiz.get("perguntas")).hasSize(2);
        assertThat(quiz.get("perguntas").get(0).get("ordem").asInt()).isEqualTo(1);
        assertThat(quiz.get("perguntas").get(1).get("ordem").asInt()).isEqualTo(2);
        assertThat(quiz.get("perguntas").get(0).get("alternativas")).hasSize(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void leituraAdministrativaTemOGabaritoComExatamenteUmaCorretaPorPergunta() {
        JsonNode criado = criarQuiz(pergunta("P1", 3, 2), pergunta("P2", 2, 0));

        JsonNode quiz = lerQuizAdmin(id(criado));

        for (JsonNode pergunta : quiz.get("perguntas")) {
            long corretas = 0;
            for (JsonNode alternativa : pergunta.get("alternativas")) {
                assertThat(alternativa.has("correta")).isTrue();
                corretas += alternativa.get("correta").asBoolean() ? 1 : 0;
            }
            assertThat(corretas).isEqualTo(1);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void leituraDoIntegranteNaoTemGabaritoDepoisDeUmaEscrita() {
        criarQuiz(pergunta("P1", 3, 1));

        JsonNode quiz = cliente(tokenPadrao())
                .get()
                .uri("/api/v1/atividades/{id}/quiz", atividadeId)
                .retrieve()
                .body(JsonNode.class);

        assertThat(quiz.get("perguntas")).hasSize(1);
        assertThat(quiz.toString()).doesNotContain("correta");
    }

    @Test
    @SuppressWarnings("unchecked")
    void atividadeQueJaTemQuizEhConflito() {
        criarQuiz(pergunta("P1", 2, 0));

        ResponseEntity<JsonNode> resposta = criar(corpoDoQuiz(atividadeId, null, pergunta("P2", 2, 0)));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("QUIZ_JA_EXISTE");
        assertThat(contar("quiz", "atividade_id", atividadeId)).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void notaMinimaForaDaFaixaEhRecusadaNaCriacaoENaAlteracao() {
        ResponseEntity<JsonNode> criacao = criar(corpoDoQuiz(atividadeId, 101, pergunta("P1", 2, 0)));
        JsonNode quiz = criarQuiz(pergunta("P1", 2, 0));
        ResponseEntity<JsonNode> alteracao =
                comoNaty(HttpMethod.PUT, "/api/v1/quizzes/" + id(quiz), Map.of("notaMinima", 101));

        assertThat(criacao.getStatusCode().value()).isEqualTo(400);
        assertThat(criacao.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        assertThat(alteracao.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void quizSemPerguntaEhRecusado() {
        ResponseEntity<JsonNode> resposta =
                criar(Map.of("atividadeId", atividadeId, "notaMinima", 70, "perguntas", java.util.List.of()));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(contar("quiz", "atividade_id", atividadeId)).isZero();
    }

    @Test
    @SuppressWarnings("unchecked")
    void atividadeInexistenteEhNaoEncontrada() {
        ResponseEntity<JsonNode> resposta = criar(corpoDoQuiz(UUID.randomUUID(), null, pergunta("P1", 2, 0)));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
    }

    @Test
    @SuppressWarnings("unchecked")
    void alteraANotaMinima() {
        JsonNode quiz = criarQuiz(pergunta("P1", 2, 0));

        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.PUT, "/api/v1/quizzes/" + id(quiz), Map.of("notaMinima", 85));

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(lerQuizAdmin(id(quiz)).get("notaMinima").asInt()).isEqualTo(85);
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeQuizSemHistoricoLevandoPerguntasEAlternativas() {
        JsonNode quiz = criarQuiz(pergunta("P1", 3, 0));
        UUID perguntaId = id(quiz.get("perguntas").get(0));

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/quizzes/" + id(quiz), null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
        assertThat(contar("quiz", "id", id(quiz))).isZero();
        assertThat(contar("pergunta", "id", perguntaId)).isZero();
        assertThat(contar("alternativa", "pergunta_id", perguntaId)).isZero();
    }

    @Test
    @SuppressWarnings("unchecked")
    void quizComTentativaNaoEhRemovido() {
        JsonNode quiz = criarQuiz(pergunta("P1", 2, 0));
        UUID perguntaId = id(quiz.get("perguntas").get(0));
        registrarTentativa(
                id(quiz),
                perguntaId,
                id(quiz.get("perguntas").get(0).get("alternativas").get(0)));

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/quizzes/" + id(quiz), null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CONTEUDO_COM_PROGRESSO");
        assertThat(contar("quiz", "id", id(quiz))).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void quizDeAtividadeComProgressoNaoEhRemovidoMesmoSemTentativa() {
        JsonNode quiz = criarQuiz(pergunta("P1", 2, 0));
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, video_assistido_em) values (?, ?, now())",
                estudanteId,
                atividadeId);

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/quizzes/" + id(quiz), null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CONTEUDO_COM_PROGRESSO");
    }

    @Test
    void quizInexistenteEhNaoEncontrado() {
        assertThat(comoNaty(HttpMethod.GET, "/api/v1/quizzes/" + UUID.randomUUID(), null)
                        .getStatusCode()
                        .value())
                .isEqualTo(404);
    }
}
