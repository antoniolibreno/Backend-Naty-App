package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AuditoriaDeConteudoApiTest extends ConteudoApiTest {

    private UUID trilhaId;
    private UUID moduloId;
    private UUID atividadeId;

    @BeforeEach
    void criarConteudo() {
        trilhaId = criarTrilha("Trilha auditada");
        moduloId = criarModulo(trilhaId, "Modulo");
        atividadeId = criarAtividade(moduloId, "Atividade");
    }

    private JsonNode alteracoesDe(UUID entidadeId) {
        return cliente(tokenNaty)
                .get()
                .uri("/api/v1/alteracoes-de-conteudo?entidadeId={id}", entidadeId)
                .retrieve()
                .body(JsonNode.class);
    }

    private List<String> acoesDe(UUID entidadeId) {
        List<String> acoes = new ArrayList<>();
        alteracoesDe(entidadeId)
                .get("itens")
                .forEach(item -> acoes.add(item.get("acao").asText()));
        return acoes;
    }

    private int registros(UUID entidadeId, String acao) {
        Integer total = jdbc.queryForObject(
                "select count(*) from alteracao_conteudo where entidade_id = ? and acao = ?",
                Integer.class,
                entidadeId,
                acao);
        return total == null ? 0 : total;
    }

    @Test
    void criacaoFicaRegistradaComContaEntidadeEMomento() {
        JsonNode item = alteracoesDe(atividadeId).get("itens").get(0);

        assertThat(item.get("usuarioId").asText()).isEqualTo(usuarioNatyId.toString());
        assertThat(item.get("entidade").asText()).isEqualTo("ATIVIDADE");
        assertThat(item.get("entidadeId").asText()).isEqualTo(atividadeId.toString());
        assertThat(item.get("acao").asText()).isEqualTo("CRIACAO");
        assertThat(OffsetDateTime.parse(item.get("ocorridaEm").asText())).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    @Test
    void alteracaoDeXpFicaRegistradaEAMaisRecenteVemPrimeiro() {
        comoNaty(HttpMethod.PUT, "/api/v1/atividades/" + atividadeId, Map.of("titulo", "Atividade", "xp", 30));

        assertThat(acoesDe(atividadeId)).containsExactly("ALTERACAO", "CRIACAO");
    }

    @Test
    void filtroPorEntidadeTrazSoOsRegistrosDela() {
        List<UUID> entidades = new ArrayList<>();
        alteracoesDe(atividadeId)
                .get("itens")
                .forEach(item ->
                        entidades.add(UUID.fromString(item.get("entidadeId").asText())));

        assertThat(entidades).isNotEmpty().containsOnly(atividadeId);
    }

    @Test
    void listagemSemFiltroTrazRegistrosDeVariasEntidades() {
        JsonNode pagina = cliente(tokenNaty)
                .get()
                .uri("/api/v1/alteracoes-de-conteudo?tamanho=100")
                .retrieve()
                .body(JsonNode.class);

        List<String> entidades = new ArrayList<>();
        pagina.get("itens").forEach(item -> entidades.add(item.get("entidade").asText()));
        assertThat(entidades).contains("TRILHA", "MODULO", "ATIVIDADE");
    }

    @Test
    void ativacaoEReordenacaoSaoRegistradas() {
        comoNaty(HttpMethod.PUT, "/api/v1/trilhas/" + trilhaId + "/ativa", Map.of("ativa", false));
        comoNaty(HttpMethod.PUT, "/api/v1/trilhas/" + trilhaId + "/ativa", Map.of("ativa", true));
        comoNaty(HttpMethod.PUT, "/api/v1/trilhas/" + trilhaId + "/modulos/ordem", Map.of("ids", List.of(moduloId)));

        assertThat(acoesDe(trilhaId)).contains("DESATIVACAO", "ATIVACAO", "REORDENACAO");
    }

    @Test
    void alteracaoRecusadaPorValidacaoNaoGeraRegistro() {
        int antes = acoesDe(atividadeId).size();

        ResponseEntity<JsonNode> resposta = comoNaty(
                HttpMethod.PUT,
                "/api/v1/atividades/" + atividadeId,
                Map.of("titulo", "x", "videoUrl", "https://exemplo.com/v", "duracaoSegundos", 10));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(acoesDe(atividadeId)).hasSize(antes);
    }

    @Test
    void remocaoRecusadaPorHistoricoNaoGeraRegistro() {
        UUID estudante = criarIntegrante(empresaDeConteudo, "Estudante", emailUnico("estudante"));
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, now())",
                estudante,
                atividadeId);

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/atividades/" + atividadeId, null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(registros(atividadeId, "REMOCAO")).isZero();
    }

    @Test
    void reordenacaoRecusadaNaoGeraRegistro() {
        ResponseEntity<JsonNode> resposta = comoNaty(
                HttpMethod.PUT,
                "/api/v1/trilhas/" + trilhaId + "/modulos/ordem",
                Map.of("ids", List.of(UUID.randomUUID())));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(registros(trilhaId, "REORDENACAO")).isZero();
    }

    @Test
    void remocaoFicaRegistradaDepoisQueOConteudoSome() {
        comoNaty(HttpMethod.DELETE, "/api/v1/atividades/" + atividadeId, null);

        assertThat(contar("atividade", "id", atividadeId)).isZero();
        assertThat(acoesDe(atividadeId)).containsExactly("REMOCAO", "CRIACAO");
    }
}
