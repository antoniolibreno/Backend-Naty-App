package com.projetointegrador.natysync.progresso;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class ProgressoApiTest extends IntegracaoTest {

    private static final UUID TRILHA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final List<UUID> ATIVIDADES_EM_ORDEM = List.of(
            UUID.fromString("00000000-0000-0000-0002-000000000001"),
            UUID.fromString("00000000-0000-0000-0002-000000000002"),
            UUID.fromString("00000000-0000-0000-0002-000000000003"),
            UUID.fromString("00000000-0000-0000-0002-000000000004"),
            UUID.fromString("00000000-0000-0000-0002-000000000005"),
            UUID.fromString("00000000-0000-0000-0002-000000000006"));

    @Autowired
    private ProgressoAtividadeRepository progressoAtividadeRepository;

    private UUID empresaId;
    private UUID integranteId;
    private UUID outroIntegranteId;
    private String token;
    private String outroToken;

    @BeforeEach
    void criarIntegrantes() {
        Empresa empresa = criarEmpresa("Empresa de Progresso");
        empresaId = empresa.getId();

        integranteId = criarIntegrante(empresa, "Integrante progresso-001", "primeiro@teste.com.br");
        outroIntegranteId = criarIntegrante(empresa, "Integrante progresso-002", "segundo@teste.com.br");

        token = autenticar("primeiro@teste.com.br", SENHA_DE_TESTE);
        outroToken = autenticar("segundo@teste.com.br", SENHA_DE_TESTE);
    }

    @AfterEach
    void limpar() {
        usuarioRepository.deleteById(integranteId);
        usuarioRepository.deleteById(outroIntegranteId);
        empresaRepository.deleteById(empresaId);
    }

    private ResponseEntity<JsonNode> lerProgresso(String tokenDoIntegrante) {
        return cliente(tokenDoIntegrante)
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    private ResponseEntity<JsonNode> assistirVideo(String tokenDoIntegrante, UUID atividadeId) {
        return cliente(tokenDoIntegrante)
                .post()
                .uri("/api/v1/atividades/{atividadeId}/video-assistido", atividadeId)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    private List<String> estadosEmOrdem(JsonNode corpo) {
        List<String> estados = new ArrayList<>();
        for (JsonNode modulo : corpo.get("modulos")) {
            for (JsonNode atividade : modulo.get("atividades")) {
                estados.add(atividade.get("estado").asText());
            }
        }
        return estados;
    }

    private Instant instante(JsonNode corpo, String campo) {
        return OffsetDateTime.parse(corpo.get(campo).asText()).toInstant();
    }

    @Test
    void integranteSemProgressoVeApenasAPrimeiraAtividadeDisponivel() {
        ResponseEntity<JsonNode> resposta = lerProgresso(token);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(estadosEmOrdem(resposta.getBody()))
                .containsExactly("DISPONIVEL", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO");
        assertThat(resposta.getBody().get("totalAtividades").asInt()).isEqualTo(6);
        assertThat(resposta.getBody().get("atividadesConcluidas").asInt()).isZero();
        assertThat(resposta.getBody().get("percentualConcluido").asInt()).isZero();
        assertThat(resposta.getBody().get("proximaAtividadeId").asText())
                .isEqualTo(ATIVIDADES_EM_ORDEM.getFirst().toString());
    }

    @Test
    void atividadeComQuizSoConcluiComTentativaAprovada() {
        for (int posicao = 0; posicao < ATIVIDADES_EM_ORDEM.size(); posicao++) {
            List<String> antes = estadosEmOrdem(lerProgresso(token).getBody());
            assertThat(antes.get(posicao)).isEqualTo("DISPONIVEL");
            if (posicao + 1 < ATIVIDADES_EM_ORDEM.size()) {
                assertThat(antes.get(posicao + 1)).isEqualTo("BLOQUEADO");
            }

            ResponseEntity<JsonNode> video = assistirVideo(token, ATIVIDADES_EM_ORDEM.get(posicao));
            assertThat(video.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(video.getBody().get("estado").asText()).isEqualTo("DISPONIVEL");
            assertThat(video.getBody().get("concluidoEm").isNull()).isTrue();

            UUID atividadeId = ATIVIDADES_EM_ORDEM.get(posicao);
            JsonNode quiz = cliente(token).get().uri("/api/v1/atividades/{id}/quiz", atividadeId)
                    .retrieve().body(JsonNode.class);
            List<java.util.Map<String, String>> respostas = new ArrayList<>();
            for (JsonNode pergunta : quiz.get("perguntas")) {
                respostas.add(java.util.Map.of(
                        "perguntaId", pergunta.get("id").asText(),
                        "alternativaId", pergunta.get("alternativas").get(0).get("id").asText()));
            }
            ResponseEntity<JsonNode> conclusao = cliente(token).post()
                    .uri("/api/v1/atividades/{id}/quiz/tentativas", atividadeId)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of("respostas", respostas)).retrieve().toEntity(JsonNode.class);
            assertThat(conclusao.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(conclusao.getBody().get("aprovado").asBoolean()).isTrue();
        }

        JsonNode fim = lerProgresso(token).getBody();
        assertThat(estadosEmOrdem(fim)).containsOnly("CONCLUIDO");
        assertThat(fim.get("atividadesConcluidas").asInt()).isEqualTo(6);
        assertThat(fim.get("percentualConcluido").asInt()).isEqualTo(100);
        assertThat(fim.get("proximaAtividadeId").isNull()).isTrue();
    }

    @Test
    void tentativaDeVideoEmAtividadeBloqueadaDevolveConflitoENaoGravaProgresso() {
        ResponseEntity<JsonNode> resposta = assistirVideo(token, ATIVIDADES_EM_ORDEM.get(1));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ATIVIDADE_BLOQUEADA");
        assertThat(progressoAtividadeRepository.findByUsuarioId(integranteId)).isEmpty();
        assertThat(estadosEmOrdem(lerProgresso(token).getBody()).get(1)).isEqualTo("BLOQUEADO");
    }

    @Test
    void registrarOMesmoVideoDuasVezesNaoDuplicaNemMoveAConclusao() {
        JsonNode primeira = assistirVideo(token, ATIVIDADES_EM_ORDEM.getFirst()).getBody();
        JsonNode segunda = assistirVideo(token, ATIVIDADES_EM_ORDEM.getFirst()).getBody();

        assertThat(instante(segunda, "concluidoEm")).isEqualTo(instante(primeira, "concluidoEm"));
        assertThat(instante(segunda, "videoAssistidoEm")).isEqualTo(instante(primeira, "videoAssistidoEm"));
        assertThat(progressoAtividadeRepository.findByUsuarioId(integranteId)).hasSize(1);
    }

    @Test
    void conclusaoLiberaAProximaAtividadeNaResposta() {
        JsonNode resposta = assistirVideo(token, ATIVIDADES_EM_ORDEM.getFirst()).getBody();

        assertThat(resposta.get("proximaAtividadeId").asText())
                .isEqualTo(ATIVIDADES_EM_ORDEM.get(1).toString());
    }

    @Test
    void progressoSemTokenEhRecusado() {
        ResponseEntity<JsonNode> resposta = clienteSemSessao()
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void progressoComTokenDesconhecidoEhRecusado() {
        ResponseEntity<JsonNode> resposta = lerProgresso("token-que-nunca-foi-emitido");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void identificadorDeIntegranteInformadoPeloClienteEhIgnorado() {
        ResponseEntity<JsonNode> resposta = cliente(token)
                .post()
                .uri("/api/v1/atividades/{atividadeId}/video-assistido", ATIVIDADES_EM_ORDEM.getFirst())
                .header("X-Integrante-Id", outroIntegranteId.toString())
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(progressoAtividadeRepository.findByUsuarioId(integranteId)).hasSize(1);
        assertThat(progressoAtividadeRepository.findByUsuarioId(outroIntegranteId))
                .isEmpty();
    }

    @Test
    void trilhaInexistenteDevolveNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = cliente(token)
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", UUID.randomUUID())
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void progressoDeUmIntegranteNaoApareceNaLeituraDeOutro() {
        assistirVideo(token, ATIVIDADES_EM_ORDEM.getFirst());

        JsonNode outro = lerProgresso(outroToken).getBody();

        assertThat(estadosEmOrdem(outro))
                .containsExactly("DISPONIVEL", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO");
        assertThat(outro.get("atividadesConcluidas").asInt()).isZero();
    }
}
