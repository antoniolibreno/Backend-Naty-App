package com.projetointegrador.natysync.progresso;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Usuario;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class ResumoProgressoApiTest extends IntegracaoTest {

    private static final UUID TRILHA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final List<UUID> ATIVIDADES = List.of(
            UUID.fromString("00000000-0000-0000-0002-000000000001"),
            UUID.fromString("00000000-0000-0000-0002-000000000002"),
            UUID.fromString("00000000-0000-0000-0002-000000000003"),
            UUID.fromString("00000000-0000-0000-0002-000000000004"),
            UUID.fromString("00000000-0000-0000-0002-000000000005"),
            UUID.fromString("00000000-0000-0000-0002-000000000006"));

    private final String emailDaAna = emailUnico("ana");
    private UUID anaId;
    private UUID brunoId;
    private UUID desativadoId;
    private UUID forasteiroId;
    private String tokenDaAna;
    private UUID trilhaExtraId;
    private UUID moduloExtraId;
    private UUID atividadeExtraId;

    @BeforeEach
    void criarCenario() {
        Empresa empresa = criarEmpresa("Empresa Resumo");
        anaId = criarIntegrante(empresa, "Ana", emailDaAna);
        brunoId = criarIntegrante(empresa, "Bruno", emailUnico("bruno"));
        desativadoId = criarIntegrante(empresa, "Carlos", emailUnico("carlos"));
        Usuario desativado = usuarioRepository.findById(desativadoId).orElseThrow();
        desativado.setAtivo(false);
        usuarioRepository.save(desativado);
        forasteiroId = criarIntegrante(criarEmpresa("Empresa Vizinha"), "Zeca", emailUnico("zeca"));
        tokenDaAna = autenticar(emailDaAna, SENHA_DE_TESTE);
    }

    @AfterEach
    void restaurarConteudo() {
        jdbc.update("update trilha set ativa = true where id = ?", TRILHA);
        if (atividadeExtraId != null) {
            jdbc.update("delete from progresso_atividade where atividade_id = ?", atividadeExtraId);
            jdbc.update("delete from atividade where id = ?", atividadeExtraId);
            jdbc.update("delete from modulo where id = ?", moduloExtraId);
            jdbc.update("delete from trilha where id = ?", trilhaExtraId);
        }
    }

    private void concluir(UUID usuarioId, List<UUID> atividades) {
        for (UUID atividadeId : atividades) {
            jdbc.update(
                    "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, now())",
                    usuarioId,
                    atividadeId);
        }
    }

    private void criarTrilhaExtra() {
        trilhaExtraId = UUID.randomUUID();
        moduloExtraId = UUID.randomUUID();
        atividadeExtraId = UUID.randomUUID();
        jdbc.update("insert into trilha (id, titulo, ordem) values (?, 'Trilha extra', 95)", trilhaExtraId);
        jdbc.update(
                "insert into modulo (id, trilha_id, titulo, ordem) values (?, ?, 'Modulo extra', 1)",
                moduloExtraId,
                trilhaExtraId);
        jdbc.update(
                "insert into atividade (id, modulo_id, titulo, ordem) values (?, ?, 'Atividade extra', 1)",
                atividadeExtraId,
                moduloExtraId);
    }

    private ResponseEntity<JsonNode> resumo(String token, UUID integranteId) {
        var cliente = token == null ? clienteSemSessao() : cliente(token);
        return cliente.get()
                .uri("/api/v1/integrantes/{id}/progresso", integranteId)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @Test
    void integranteSemEstudoTemPrimeiraAtividadeComoProxima() {
        ResponseEntity<JsonNode> resposta = resumo(tokenDaAna, anaId);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        JsonNode corpo = resposta.getBody();
        assertThat(corpo.get("integranteId").asText()).isEqualTo(anaId.toString());
        assertThat(corpo.get("totalAtividades").asInt()).isEqualTo(6);
        assertThat(corpo.get("atividadesConcluidas").asInt()).isZero();
        assertThat(corpo.get("percentualConcluido").asInt()).isZero();
        assertThat(corpo.get("proximaAtividade").get("atividadeId").asText())
                .isEqualTo(ATIVIDADES.get(0).toString());
        assertThat(corpo.get("proximaAtividade").get("trilhaId").asText()).isEqualTo(TRILHA.toString());
        assertThat(corpo.get("proximaAtividade").get("titulo").asText()).isNotBlank();
    }

    @Test
    void progressoParcialIndicaAProximaAtividade() {
        concluir(anaId, ATIVIDADES.subList(0, 2));

        JsonNode corpo = resumo(tokenDaAna, anaId).getBody();

        assertThat(corpo.get("atividadesConcluidas").asInt()).isEqualTo(2);
        assertThat(corpo.get("percentualConcluido").asInt()).isEqualTo(33);
        assertThat(corpo.get("proximaAtividade").get("atividadeId").asText())
                .isEqualTo(ATIVIDADES.get(2).toString());
    }

    @Test
    void tudoConcluidoNaoTemProximaAtividade() {
        concluir(anaId, ATIVIDADES);

        JsonNode corpo = resumo(tokenDaAna, anaId).getBody();

        assertThat(corpo.get("atividadesConcluidas").asInt()).isEqualTo(6);
        assertThat(corpo.get("percentualConcluido").asInt()).isEqualTo(100);
        assertThat(corpo.get("proximaAtividade").isNull()).isTrue();
    }

    @Test
    void respostaNaoTemModulosNemAtividades() {
        JsonNode corpo = resumo(tokenDaAna, anaId).getBody();

        assertThat(corpo.has("modulos")).isFalse();
        assertThat(corpo.has("atividades")).isFalse();
    }

    @Test
    void coincideComALeituraDaTrilha() {
        concluir(anaId, ATIVIDADES.subList(0, 4));

        JsonNode resumo = resumo(tokenDaAna, anaId).getBody();
        JsonNode trilha = cliente(tokenDaAna)
                .get()
                .uri("/api/v1/trilhas/{id}/progresso", TRILHA)
                .retrieve()
                .body(JsonNode.class);

        assertThat(resumo.get("totalAtividades").asInt())
                .isEqualTo(trilha.get("totalAtividades").asInt());
        assertThat(resumo.get("atividadesConcluidas").asInt())
                .isEqualTo(trilha.get("atividadesConcluidas").asInt());
        assertThat(resumo.get("percentualConcluido").asInt())
                .isEqualTo(trilha.get("percentualConcluido").asInt());
        assertThat(resumo.get("proximaAtividade").get("atividadeId").asText())
                .isEqualTo(trilha.get("proximaAtividadeId").asText());
    }

    @Test
    void proximaAtividadeSegueAOrdemDasTrilhas() {
        criarTrilhaExtra();
        concluir(anaId, ATIVIDADES);

        JsonNode corpo = resumo(tokenDaAna, anaId).getBody();

        assertThat(corpo.get("totalAtividades").asInt()).isEqualTo(7);
        assertThat(corpo.get("atividadesConcluidas").asInt()).isEqualTo(6);
        assertThat(corpo.get("proximaAtividade").get("atividadeId").asText()).isEqualTo(atividadeExtraId.toString());
        assertThat(corpo.get("proximaAtividade").get("trilhaId").asText()).isEqualTo(trilhaExtraId.toString());
    }

    @Test
    void trilhaInativaFicaForaDaSomaEDaProximaAtividade() {
        criarTrilhaExtra();
        concluir(anaId, ATIVIDADES.subList(0, 1));
        jdbc.update("update trilha set ativa = false where id = ?", TRILHA);

        JsonNode corpo = resumo(tokenDaAna, anaId).getBody();

        assertThat(corpo.get("totalAtividades").asInt()).isEqualTo(1);
        assertThat(corpo.get("atividadesConcluidas").asInt()).isZero();
        assertThat(corpo.get("proximaAtividade").get("atividadeId").asText()).isEqualTo(atividadeExtraId.toString());
    }

    @Test
    void colegaAtivoDaMesmaEmpresaDevolveOResumoDoColega() {
        concluir(brunoId, ATIVIDADES.subList(0, 3));

        JsonNode corpo = resumo(tokenDaAna, brunoId).getBody();

        assertThat(corpo.get("integranteId").asText()).isEqualTo(brunoId.toString());
        assertThat(corpo.get("atividadesConcluidas").asInt()).isEqualTo(3);
        assertThat(corpo.get("percentualConcluido").asInt()).isEqualTo(50);
    }

    @Test
    void outraEmpresaDesativadoEInexistenteRespondemIgual() {
        ResponseEntity<JsonNode> outraEmpresa = resumo(tokenDaAna, forasteiroId);
        ResponseEntity<JsonNode> desativado = resumo(tokenDaAna, desativadoId);
        ResponseEntity<JsonNode> inexistente = resumo(tokenDaAna, UUID.randomUUID());

        for (ResponseEntity<JsonNode> resposta : List.of(outraEmpresa, desativado, inexistente)) {
            assertThat(resposta.getStatusCode().value()).isEqualTo(404);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
        }
    }

    @Test
    void semTokenEhRecusado() {
        ResponseEntity<JsonNode> resposta = resumo(null, anaId);

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }
}
