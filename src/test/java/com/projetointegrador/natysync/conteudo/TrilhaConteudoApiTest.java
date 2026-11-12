package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class TrilhaConteudoApiTest extends ConteudoApiTest {

    private boolean listagemTem(UUID trilhaId) {
        JsonNode trilhas =
                cliente(tokenNaty).get().uri("/api/v1/trilhas").retrieve().body(JsonNode.class);
        for (JsonNode trilha : trilhas) {
            if (trilha.get("id").asText().equals(trilhaId.toString())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void criaTrilhaAtivaNoFimEElaApareceNaListagem() {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.POST, "/api/v1/trilhas", Map.of("titulo", "  Trilha nova  ", "descricao", "d"));
        UUID id = UUID.fromString(resposta.getBody().get("id").asText());
        registrarTrilhaCriadaPorSql(id);

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        assertThat(resposta.getBody().get("titulo").asText()).isEqualTo("Trilha nova");
        assertThat(resposta.getBody().get("ativa").asBoolean()).isTrue();
        assertThat(resposta.getBody().get("ordem").asInt()).isGreaterThan(1);
        assertThat(listagemTem(id)).isTrue();
    }

    @Test
    void tituloAusenteOuEmBrancoEhErroDeValidacao() {
        for (Map<String, Object> corpo : new Map[] {Map.of(), Map.of("titulo", "   ")}) {
            ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.POST, "/api/v1/trilhas", corpo);

            assertThat(resposta.getStatusCode().value()).isEqualTo(400);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        }
    }

    @Test
    void tituloAcimaDe255CaracteresEhErroDeValidacao() {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.POST, "/api/v1/trilhas", Map.of("titulo", "x".repeat(256)));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void alteraTituloEDescricao() {
        UUID id = criarTrilha("Antes");

        ResponseEntity<JsonNode> resposta = comoNaty(
                HttpMethod.PUT, "/api/v1/trilhas/" + id, Map.of("titulo", "Depois", "descricao", "nova descricao"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(lerTrilha(id).get("titulo").asText()).isEqualTo("Depois");
        assertThat(lerTrilha(id).get("descricao").asText()).isEqualTo("nova descricao");
    }

    @Test
    void trilhaInexistenteEhNaoEncontradaEmAlteracaoEmRemocaoEEmAtivacao() {
        String inexistente = "/api/v1/trilhas/" + UUID.randomUUID();

        for (ResponseEntity<JsonNode> resposta : java.util.List.of(
                comoNaty(HttpMethod.PUT, inexistente, Map.of("titulo", "x")),
                comoNaty(HttpMethod.DELETE, inexistente, null),
                comoNaty(HttpMethod.PUT, inexistente + "/ativa", Map.of("ativa", false)))) {
            assertThat(resposta.getStatusCode().value()).isEqualTo(404);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
        }
    }

    @Test
    void removeTrilhaSemHistoricoComModulosEAtividades() {
        UUID trilhaId = criarTrilha("Para remover");
        UUID moduloId = criarModulo(trilhaId, "Modulo");
        UUID atividadeId = criarAtividade(moduloId, "Atividade");

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/trilhas/" + trilhaId, null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
        assertThat(contar("trilha", "id", trilhaId)).isZero();
        assertThat(contar("modulo", "id", moduloId)).isZero();
        assertThat(contar("atividade", "id", atividadeId)).isZero();
    }
}
