package com.projetointegrador.natysync.progresso;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class TentativaQuizApiTest extends IntegracaoTest {

    private static final UUID ATIVIDADE = UUID.fromString("00000000-0000-0000-0002-000000000001");
    private static final UUID BLOQUEADA = UUID.fromString("00000000-0000-0000-0002-000000000002");
    private String token;
    private UUID integranteId;

    @BeforeEach
    void preparar() {
        var empresa = criarEmpresa("Empresa quiz " + UUID.randomUUID());
        String email = "quiz-" + UUID.randomUUID() + "@teste.com.br";
        integranteId = criarIntegrante(empresa, "Integrante Quiz", email);
        token = autenticar(email, SENHA_DE_TESTE);
    }

    private JsonNode quiz() {
        return cliente(token).get().uri("/api/v1/atividades/{id}/quiz", ATIVIDADE).retrieve().body(JsonNode.class);
    }

    private List<Map<String, String>> respostas(boolean todasCorretas) {
        List<Map<String, String>> resultado = new ArrayList<>();
        JsonNode perguntas = quiz().get("perguntas");
        for (JsonNode pergunta : perguntas) {
            JsonNode alternativas = pergunta.get("alternativas");
            int indice = todasCorretas ? 0 : alternativas.size() - 1;
            resultado.add(Map.of("perguntaId", pergunta.get("id").asText(),
                    "alternativaId", alternativas.get(indice).get("id").asText()));
        }
        return resultado;
    }

    private ResponseEntity<JsonNode> enviar(List<Map<String, String>> respostas) {
        return cliente(token).post().uri("/api/v1/atividades/{id}/quiz/tentativas", ATIVIDADE)
                .contentType(MediaType.APPLICATION_JSON).body(Map.of("respostas", respostas))
                .retrieve().toEntity(JsonNode.class);
    }

    @Test
    void corrigeApenasNoServidorEConclusaoExigeNotaMinima() {
        var reprovada = enviar(respostas(false));
        assertThat(reprovada.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reprovada.getBody().get("nota").asInt()).isZero();
        assertThat(reprovada.getBody().get("aprovado").asBoolean()).isFalse();
        assertThat(reprovada.getBody().get("concluidoEm").isNull()).isTrue();

        var aprovada = enviar(respostas(true));
        assertThat(aprovada.getBody().get("nota").asInt()).isEqualTo(100);
        assertThat(aprovada.getBody().get("aprovado").asBoolean()).isTrue();
        assertThat(aprovada.getBody().get("concluidoEm").isNull()).isFalse();
        assertThat(progressoDeTeste()).isEqualTo(1);
    }

    private int progressoDeTeste() {
        return jdbc.queryForObject("select count(*) from progresso_atividade where usuario_id = ? and atividade_id = ? and concluido_em is not null",
                Integer.class, integranteId, ATIVIDADE);
    }

    @Test
    void tentativasSaoIlimitadasEOVideoNaoConcluiAtividadeComQuiz() {
        var video = cliente(token).post().uri("/api/v1/atividades/{id}/video-assistido", ATIVIDADE)
                .retrieve().toEntity(JsonNode.class);
        assertThat(video.getBody().get("estado").asText()).isEqualTo("DISPONIVEL");
        assertThat(video.getBody().get("videoAssistidoEm").isNull()).isFalse();
        assertThat(video.getBody().get("concluidoEm").isNull()).isTrue();

        enviar(respostas(false));
        var aprovada = enviar(respostas(true));
        assertThat(aprovada.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jdbc.queryForObject("select count(*) from tentativa_quiz where usuario_id = ?", Integer.class, integranteId))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from resposta_tentativa r join tentativa_quiz t on t.id = r.tentativa_id where t.usuario_id = ?",
                Integer.class, integranteId)).isEqualTo(8);
    }

    @Test
    void retentativaDepoisDeAprovadoNaoMoveConclusaoENemPerdeHistorico() {
        var aprovada = enviar(respostas(true)).getBody();
        var reprovada = enviar(respostas(false)).getBody();
        assertThat(reprovada.get("concluidoEm").asText()).isEqualTo(aprovada.get("concluidoEm").asText());
        assertThat(progressoDeTeste()).isEqualTo(1);
    }

    @Test
    void progressoMantemAMelhorNotaEntreTentativas() {
        enviar(respostas(true));
        enviar(respostas(false));
        assertThat(jdbc.queryForObject(
                "select melhor_nota from progresso_atividade where usuario_id = ? and atividade_id = ?",
                Integer.class, integranteId, ATIVIDADE)).isEqualTo(100);
    }

    @Test
    void rejeitaPerguntaAusenteRepetidaDeOutroQuizEAlternativaDeOutraPergunta() {
        List<Map<String, String>> corretas = respostas(true);
        assertThat(enviar(corretas.subList(0, corretas.size() - 1)).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        List<Map<String, String>> repetidas = new ArrayList<>(corretas);
        repetidas.set(1, corretas.getFirst());
        assertThat(enviar(repetidas).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        List<Map<String, String>> deOutroQuiz = new ArrayList<>(corretas);
        deOutroQuiz.set(0, Map.of("perguntaId", UUID.randomUUID().toString(),
                "alternativaId", quiz().get("perguntas").get(0).get("alternativas").get(0).get("id").asText()));
        assertThat(enviar(deOutroQuiz).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        List<Map<String, String>> alternativaErrada = new ArrayList<>(corretas);
        alternativaErrada.set(0, Map.of("perguntaId", corretas.get(0).get("perguntaId"),
                "alternativaId", quiz().get("perguntas").get(1).get("alternativas").get(0).get("id").asText()));
        assertThat(enviar(alternativaErrada).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(jdbc.queryForObject("select count(*) from tentativa_quiz where usuario_id = ?", Integer.class, integranteId))
                .isZero();
    }

    @Test
    void tentativaDeAtividadeBloqueadaEhRecusada() {
        var respostas = respostas(true);
        var resposta = cliente(token).post().uri("/api/v1/atividades/{id}/quiz/tentativas", BLOQUEADA)
                .contentType(MediaType.APPLICATION_JSON).body(Map.of("respostas", respostas))
                .retrieve().toEntity(JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(jdbc.queryForObject("select count(*) from tentativa_quiz where usuario_id = ?", Integer.class, integranteId))
                .isZero();
    }

    @Test
    void tentativaELeituraDoQuizNaoRevelamGabarito() {
        var resposta = enviar(respostas(false));
        String serializado = resposta.getBody().toString();
        assertThat(serializado).doesNotContain("correta").doesNotContain("alternativaCorreta");
        assertThat(serializado).contains("perguntasErradas");

        String leitura = quiz().toString();
        assertThat(leitura).doesNotContain("correta").doesNotContain("alternativaCorreta");
        for (JsonNode pergunta : quiz().get("perguntas")) {
            for (JsonNode alternativa : pergunta.get("alternativas")) {
                assertThat(alternativa.has("correta")).isFalse();
            }
        }
    }
}
