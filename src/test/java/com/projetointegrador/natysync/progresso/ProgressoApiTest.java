package com.projetointegrador.natysync.progresso;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
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
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProgressoAtividadeRepository progressoAtividadeRepository;

    private UUID empresaId;
    private UUID integranteId;
    private UUID outroIntegranteId;

    @BeforeEach
    void criarIntegrantes() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setNome("Empresa de Progresso");
        empresa.setAtiva(true);
        empresa.setCriadoEm(OffsetDateTime.now());
        empresa.setAtualizadoEm(OffsetDateTime.now());
        empresaId = empresaRepository.save(empresa).getId();

        integranteId = criarIntegrante(empresa, "progresso-001", "primeiro@teste.com.br");
        outroIntegranteId = criarIntegrante(empresa, "progresso-002", "segundo@teste.com.br");
    }

    @AfterEach
    void limpar() {
        usuarioRepository.deleteById(integranteId);
        usuarioRepository.deleteById(outroIntegranteId);
        empresaRepository.deleteById(empresaId);
    }

    private UUID criarIntegrante(Empresa empresa, String natyId, String email) {
        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNatyId(natyId);
        usuario.setNome("Integrante " + natyId);
        usuario.setEmail(email);
        usuario.setPerfil("user");
        usuario.setStatus("offline");
        usuario.setPayload("{}");
        return usuarioRepository.save(usuario).getId();
    }

    private ResponseEntity<JsonNode> lerProgresso(UUID integrante) {
        return cliente()
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                .header("X-Integrante-Id", integrante.toString())
                .retrieve()
                .toEntity(JsonNode.class);
    }

    private ResponseEntity<JsonNode> assistirVideo(UUID integrante, UUID atividadeId) {
        return cliente()
                .post()
                .uri("/api/v1/atividades/{atividadeId}/video-assistido", atividadeId)
                .header("X-Integrante-Id", integrante.toString())
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
        ResponseEntity<JsonNode> resposta = lerProgresso(integranteId);

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
    void cadaConclusaoLiberaASeguinteAteOFimDaTrilha() {
        for (int posicao = 0; posicao < ATIVIDADES_EM_ORDEM.size(); posicao++) {
            List<String> antes = estadosEmOrdem(lerProgresso(integranteId).getBody());
            assertThat(antes.get(posicao)).isEqualTo("DISPONIVEL");
            if (posicao + 1 < ATIVIDADES_EM_ORDEM.size()) {
                assertThat(antes.get(posicao + 1)).isEqualTo("BLOQUEADO");
            }

            ResponseEntity<JsonNode> conclusao = assistirVideo(integranteId, ATIVIDADES_EM_ORDEM.get(posicao));
            assertThat(conclusao.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(conclusao.getBody().get("estado").asText()).isEqualTo("CONCLUIDO");
        }

        JsonNode fim = lerProgresso(integranteId).getBody();
        assertThat(estadosEmOrdem(fim)).containsOnly("CONCLUIDO");
        assertThat(fim.get("atividadesConcluidas").asInt()).isEqualTo(6);
        assertThat(fim.get("percentualConcluido").asInt()).isEqualTo(100);
        assertThat(fim.get("proximaAtividadeId").isNull()).isTrue();
    }

    @Test
    void videoAssistidoEmAtividadeBloqueadaDevolveConflitoENaoGravaProgresso() {
        ResponseEntity<JsonNode> resposta = assistirVideo(integranteId, ATIVIDADES_EM_ORDEM.get(1));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ATIVIDADE_BLOQUEADA");
        assertThat(progressoAtividadeRepository.findByUsuarioId(integranteId)).isEmpty();
        assertThat(estadosEmOrdem(lerProgresso(integranteId).getBody()).get(1)).isEqualTo("BLOQUEADO");
    }

    @Test
    void registrarOMesmoVideoDuasVezesNaoDuplicaNemMoveAConclusao() {
        JsonNode primeira =
                assistirVideo(integranteId, ATIVIDADES_EM_ORDEM.getFirst()).getBody();
        JsonNode segunda =
                assistirVideo(integranteId, ATIVIDADES_EM_ORDEM.getFirst()).getBody();

        assertThat(instante(segunda, "concluidoEm")).isEqualTo(instante(primeira, "concluidoEm"));
        assertThat(instante(segunda, "videoAssistidoEm")).isEqualTo(instante(primeira, "videoAssistidoEm"));
        assertThat(segunda.get("concluidoEm").asText())
                .isEqualTo(primeira.get("concluidoEm").asText());
        assertThat(progressoAtividadeRepository.findByUsuarioId(integranteId)).hasSize(1);
    }

    @Test
    void conclusaoLiberaAProximaAtividadeNaResposta() {
        JsonNode resposta =
                assistirVideo(integranteId, ATIVIDADES_EM_ORDEM.getFirst()).getBody();

        assertThat(resposta.get("proximaAtividadeId").asText())
                .isEqualTo(ATIVIDADES_EM_ORDEM.get(1).toString());
    }

    @Test
    void cabecalhoDeIntegranteAusenteDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = cliente()
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("INTEGRANTE_NAO_INFORMADO");
    }

    @Test
    void cabecalhoDeIntegranteMalformadoDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = cliente()
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", TRILHA)
                .header("X-Integrante-Id", "nao-e-identificador")
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("INTEGRANTE_NAO_INFORMADO");
    }

    @Test
    void integranteInexistenteDevolveNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = lerProgresso(UUID.randomUUID());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
    }

    @Test
    void trilhaInexistenteDevolveNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = cliente()
                .get()
                .uri("/api/v1/trilhas/{trilhaId}/progresso", UUID.randomUUID())
                .header("X-Integrante-Id", integranteId.toString())
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void progressoDeUmIntegranteNaoApareceNaLeituraDeOutro() {
        assistirVideo(integranteId, ATIVIDADES_EM_ORDEM.getFirst());

        JsonNode outro = lerProgresso(outroIntegranteId).getBody();

        assertThat(estadosEmOrdem(outro))
                .containsExactly("DISPONIVEL", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO", "BLOQUEADO");
        assertThat(outro.get("atividadesConcluidas").asInt()).isZero();
    }
}
