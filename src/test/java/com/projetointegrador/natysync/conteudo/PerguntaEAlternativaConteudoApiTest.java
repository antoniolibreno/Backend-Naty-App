package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class PerguntaEAlternativaConteudoApiTest extends BaseDeQuizApiTest {

    private JsonNode quiz;
    private UUID quizId;
    private UUID perguntaId;
    private List<UUID> alternativas;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void criarQuizBase() {
        prepararAtividade();
        quiz = criarQuiz(pergunta("P1", 3, 0), pergunta("P2", 2, 1));
        quizId = id(quiz);
        JsonNode primeira = quiz.get("perguntas").get(0);
        perguntaId = id(primeira);
        alternativas = new ArrayList<>();
        primeira.get("alternativas").forEach(a -> alternativas.add(id(a)));
    }

    private ResponseEntity<JsonNode> criarPergunta(Map<String, Object> corpo) {
        return comoNaty(HttpMethod.POST, "/api/v1/perguntas", corpo);
    }

    private ResponseEntity<JsonNode> criarAlternativa(UUID pergunta, String texto, boolean correta) {
        return comoNaty(
                HttpMethod.POST,
                "/api/v1/alternativas",
                Map.of("perguntaId", pergunta, "texto", texto, "correta", correta));
    }

    private ResponseEntity<JsonNode> alterarAlternativa(UUID id, String texto, boolean correta) {
        return comoNaty(HttpMethod.PUT, "/api/v1/alternativas/" + id, Map.of("texto", texto, "correta", correta));
    }

    private boolean ehCorreta(UUID alternativaId) {
        Boolean correta =
                jdbc.queryForObject("select correta from alternativa where id = ?", Boolean.class, alternativaId);
        return Boolean.TRUE.equals(correta);
    }

    private Map<String, Object> corpoDePergunta(String enunciado, List<Map<String, Object>> alternativas) {
        return Map.of("quizId", quizId, "enunciado", enunciado, "alternativas", alternativas);
    }

    @Test
    void criaPerguntaNoFimDoQuizComAlternativasNumeradas() {
        ResponseEntity<JsonNode> resposta = criarPergunta(corpoDePergunta(
                "P3", List.of(alternativa("a", false), alternativa("b", true), alternativa("c", false))));

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        assertThat(resposta.getBody().get("ordem").asInt()).isEqualTo(3);
        assertThat(resposta.getBody().get("alternativas")).hasSize(3);
        assertThat(corretasDaPergunta(id(resposta.getBody()))).isEqualTo(1);
    }

    @Test
    void perguntaSemCorretaOuComDuasCorretasOuComUmaAlternativaEhRecusada() {
        int antes = contar("pergunta", "quiz_id", quizId);

        for (List<Map<String, Object>> alternativasInvalidas : List.of(
                List.of(alternativa("a", false), alternativa("b", false)),
                List.of(alternativa("a", true), alternativa("b", true)),
                List.of(alternativa("a", true)))) {
            ResponseEntity<JsonNode> resposta = criarPergunta(corpoDePergunta("P3", alternativasInvalidas));

            assertThat(resposta.getStatusCode().value()).isEqualTo(400);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        }
        assertThat(contar("pergunta", "quiz_id", quizId)).isEqualTo(antes);
    }

    @Test
    void enunciadoAusenteEhRecusado() {
        ResponseEntity<JsonNode> resposta = criarPergunta(
                Map.of("quizId", quizId, "alternativas", List.of(alternativa("a", true), alternativa("b", false))));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void perguntaEmQuizInexistenteEhNaoEncontrada() {
        ResponseEntity<JsonNode> resposta = criarPergunta(Map.of(
                "quizId", UUID.randomUUID(),
                "enunciado", "x",
                "alternativas", List.of(alternativa("a", true), alternativa("b", false))));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void alteraOEnunciado() {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.PUT, "/api/v1/perguntas/" + perguntaId, Map.of("enunciado", "Novo enunciado"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().get("enunciado").asText()).isEqualTo("Novo enunciado");
    }

    @Test
    void removeAPerguntaEOutraAindaPodeSerRemovidaAteSobrarUma() {
        UUID segunda = id(quiz.get("perguntas").get(1));

        assertThat(comoNaty(HttpMethod.DELETE, "/api/v1/perguntas/" + perguntaId, null)
                        .getStatusCode()
                        .value())
                .isEqualTo(204);
        ResponseEntity<JsonNode> ultima = comoNaty(HttpMethod.DELETE, "/api/v1/perguntas/" + segunda, null);

        assertThat(ultima.getStatusCode().value()).isEqualTo(409);
        assertThat(ultima.getBody().get("codigo").asText()).isEqualTo("QUIZ_SEM_PERGUNTA");
        assertThat(contar("pergunta", "id", segunda)).isEqualTo(1);
    }

    @Test
    void perguntaJaRespondidaNaoEhRemovida() {
        registrarTentativa(quizId, perguntaId, alternativas.get(1));

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/perguntas/" + perguntaId, null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CONTEUDO_COM_PROGRESSO");
    }

    @Test
    void alternativaIncorretaNovaNaoMudaOGabarito() {
        ResponseEntity<JsonNode> resposta = criarAlternativa(perguntaId, "Nova", false);

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        assertThat(resposta.getBody().get("ordem").asInt()).isEqualTo(4);
        assertThat(ehCorreta(alternativas.get(0))).isTrue();
        assertThat(corretasDaPergunta(perguntaId)).isEqualTo(1);
    }

    @Test
    void alternativaCorretaNovaMoveAMarcaDaAnterior() {
        ResponseEntity<JsonNode> resposta = criarAlternativa(perguntaId, "Nova correta", true);

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        assertThat(resposta.getBody().get("correta").asBoolean()).isTrue();
        assertThat(ehCorreta(alternativas.get(0))).isFalse();
        assertThat(corretasDaPergunta(perguntaId)).isEqualTo(1);
    }

    @Test
    void marcarOutraAlternativaMoveAMarca() {
        ResponseEntity<JsonNode> resposta = alterarAlternativa(alternativas.get(2), "Alternativa 3", true);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(ehCorreta(alternativas.get(2))).isTrue();
        assertThat(ehCorreta(alternativas.get(0))).isFalse();
        assertThat(corretasDaPergunta(perguntaId)).isEqualTo(1);
    }

    @Test
    void alterarSoOTextoDaCorretaMantemAMarca() {
        ResponseEntity<JsonNode> resposta = alterarAlternativa(alternativas.get(0), "Texto novo", true);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().get("texto").asText()).isEqualTo("Texto novo");
        assertThat(ehCorreta(alternativas.get(0))).isTrue();
    }

    @Test
    void desmarcarAUnicaCorretaEhConflitoENaoAlteraNada() {
        ResponseEntity<JsonNode> resposta = alterarAlternativa(alternativas.get(0), "Alternativa 1", false);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("GABARITO_INVALIDO");
        assertThat(ehCorreta(alternativas.get(0))).isTrue();
    }

    @Test
    void removerACorretaEhConflito() {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.DELETE, "/api/v1/alternativas/" + alternativas.get(0), null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("GABARITO_INVALIDO");
        assertThat(contar("alternativa", "id", alternativas.get(0))).isEqualTo(1);
    }

    @Test
    void removerAbaixoDoMinimoDeDuasEhConflito() {
        UUID segunda = id(quiz.get("perguntas").get(1));
        UUID incorreta = null;
        for (JsonNode alternativa : quiz.get("perguntas").get(1).get("alternativas")) {
            if (!alternativa.get("correta").asBoolean()) {
                incorreta = id(alternativa);
            }
        }

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/alternativas/" + incorreta, null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("GABARITO_INVALIDO");
        assertThat(contar("alternativa", "pergunta_id", segunda)).isEqualTo(2);
    }

    @Test
    void removeAlternativaIncorretaQuandoSobramAoMenosDuas() {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.DELETE, "/api/v1/alternativas/" + alternativas.get(2), null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
        assertThat(contar("alternativa", "pergunta_id", perguntaId)).isEqualTo(2);
        assertThat(corretasDaPergunta(perguntaId)).isEqualTo(1);
    }

    @Test
    void alternativaJaEscolhidaNaoEhRemovida() {
        registrarTentativa(quizId, perguntaId, alternativas.get(2));

        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.DELETE, "/api/v1/alternativas/" + alternativas.get(2), null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CONTEUDO_COM_PROGRESSO");
    }

    @Test
    void alternativaEmPerguntaOuIdInexistenteEhNaoEncontrada() {
        assertThat(criarAlternativa(UUID.randomUUID(), "x", false)
                        .getStatusCode()
                        .value())
                .isEqualTo(404);
        assertThat(alterarAlternativa(UUID.randomUUID(), "x", false)
                        .getStatusCode()
                        .value())
                .isEqualTo(404);
        assertThat(comoNaty(HttpMethod.DELETE, "/api/v1/alternativas/" + UUID.randomUUID(), null)
                        .getStatusCode()
                        .value())
                .isEqualTo(404);
    }

    @Test
    void marcacoesConcorrentesDeAlternativasDiferentesDeixamExatamenteUmaCorreta() throws Exception {
        int chamadas = alternativas.size();
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(chamadas);
        try {
            List<Future<Integer>> resultados = new ArrayList<>();
            for (UUID alvo : alternativas) {
                resultados.add(executor.submit(() -> {
                    largada.await();
                    return alterarAlternativa(alvo, "Alternativa", true)
                            .getStatusCode()
                            .value();
                }));
            }
            largada.countDown();
            for (Future<Integer> resultado : resultados) {
                assertThat(resultado.get()).isEqualTo(200);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(corretasDaPergunta(perguntaId)).isEqualTo(1);
    }

    @Test
    void trocaDeGabaritoGeraUmRegistroPorAlternativa() {
        alterarAlternativa(alternativas.get(2), "Alternativa 3", true);

        for (UUID alternativa : List.of(alternativas.get(0), alternativas.get(2))) {
            JsonNode alteracoes = cliente(tokenNaty)
                    .get()
                    .uri("/api/v1/alteracoes-de-conteudo?entidadeId={id}", alternativa)
                    .retrieve()
                    .body(JsonNode.class);
            List<String> acoes = new ArrayList<>();
            alteracoes.get("itens").forEach(item -> acoes.add(item.get("acao").asText()));
            assertThat(acoes).as(alternativa.toString()).contains("ALTERACAO", "CRIACAO");
            assertThat(alteracoes.get("itens").get(0).get("entidade").asText()).isEqualTo("ALTERNATIVA");
            assertThat(alteracoes.get("itens").get(0).get("usuarioId").asText()).isEqualTo(usuarioNatyId.toString());
        }
    }

    @Test
    void criacaoDoQuizRegistraQuizPerguntasEAlternativas() {
        for (UUID entidade : List.of(quizId, perguntaId, alternativas.get(0))) {
            Integer total = jdbc.queryForObject(
                    "select count(*) from alteracao_conteudo where entidade_id = ? and acao = 'CRIACAO'",
                    Integer.class,
                    entidade);
            assertThat(total).as(entidade.toString()).isEqualTo(1);
        }
    }

    @Test
    void escritaRecusadaNaoGeraRegistro() {
        alterarAlternativa(alternativas.get(0), "x", false);

        Integer total = jdbc.queryForObject(
                "select count(*) from alteracao_conteudo where entidade_id = ? and acao = 'ALTERACAO'",
                Integer.class,
                alternativas.get(0));
        assertThat(total).isZero();
    }
}
