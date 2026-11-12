package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class OrdemDeConteudoApiTest extends ConteudoApiTest {

    private UUID trilhaId;
    private UUID m1;
    private UUID m2;
    private UUID m3;
    private UUID a1;
    private UUID a2;
    private UUID a3;

    @BeforeEach
    void criarEstrutura() {
        trilhaId = criarTrilha("Trilha de ordem");
        m1 = criarModulo(trilhaId, "M1");
        m2 = criarModulo(trilhaId, "M2");
        m3 = criarModulo(trilhaId, "M3");
        a1 = criarAtividade(m1, "A1");
        a2 = criarAtividade(m1, "A2");
        a3 = criarAtividade(m1, "A3");
    }

    private ResponseEntity<JsonNode> reordenarModulos(List<UUID> ids) {
        return comoNaty(HttpMethod.PUT, "/api/v1/trilhas/" + trilhaId + "/modulos/ordem", Map.of("ids", ids));
    }

    private ResponseEntity<JsonNode> reordenarAtividades(List<UUID> ids) {
        return comoNaty(HttpMethod.PUT, "/api/v1/modulos/" + m1 + "/atividades/ordem", Map.of("ids", ids));
    }

    private List<String> titulosDasAtividades() {
        List<String> titulos = new ArrayList<>();
        lerTrilha(trilhaId)
                .get("modulos")
                .get(0)
                .get("atividades")
                .forEach(a -> titulos.add(a.get("titulo").asText()));
        return titulos;
    }

    @Test
    void reordenaModulosNaOrdemPedidaSemLacuna() {
        ResponseEntity<JsonNode> resposta = reordenarModulos(List.of(m3, m1, m2));

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
        assertThat(titulosDosModulos(trilhaId)).containsExactly("M3", "M1", "M2");
        assertThat(jdbc.queryForList(
                        "select ordem from modulo where trilha_id = ? order by ordem", Integer.class, trilhaId))
                .containsExactly(1, 2, 3);
    }

    @Test
    void reordenaAtividadesNaOrdemPedida() {
        ResponseEntity<JsonNode> resposta = reordenarAtividades(List.of(a3, a2, a1));

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
        assertThat(titulosDasAtividades()).containsExactly("A3", "A2", "A1");
    }

    @Test
    void listaIncompletaEhConflitoENaoAlteraAOrdem() {
        ResponseEntity<JsonNode> resposta = reordenarModulos(List.of(m3, m1));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ORDEM_INVALIDA");
        assertThat(titulosDosModulos(trilhaId)).containsExactly("M1", "M2", "M3");
    }

    @Test
    void listaComRepeticaoOuSobraEhConflito() {
        assertThat(reordenarModulos(List.of(m1, m1, m2)).getStatusCode().value())
                .isEqualTo(409);
        assertThat(reordenarModulos(List.of(m1, m2, m3, UUID.randomUUID()))
                        .getStatusCode()
                        .value())
                .isEqualTo(409);
        assertThat(titulosDosModulos(trilhaId)).containsExactly("M1", "M2", "M3");
    }

    @Test
    void atividadeDeOutroModuloEhConflito() {
        UUID deOutroModulo = criarAtividade(m2, "B1");

        ResponseEntity<JsonNode> resposta = reordenarAtividades(List.of(a1, a2, deOutroModulo));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ORDEM_INVALIDA");
        assertThat(titulosDasAtividades()).containsExactly("A1", "A2", "A3");
    }

    @Test
    void paiInexistenteEhNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = comoNaty(
                HttpMethod.PUT, "/api/v1/trilhas/" + UUID.randomUUID() + "/modulos/ordem", Map.of("ids", List.of(m1)));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void listaVaziaEhErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = reordenarModulos(List.of());

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void criacaoDepoisDaReordenacaoVaiParaOFim() {
        reordenarAtividades(List.of(a3, a1, a2));
        criarAtividade(m1, "A4");

        assertThat(titulosDasAtividades()).containsExactly("A3", "A1", "A2", "A4");
    }
}
