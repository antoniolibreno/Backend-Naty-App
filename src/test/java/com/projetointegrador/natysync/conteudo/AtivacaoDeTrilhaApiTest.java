package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AtivacaoDeTrilhaApiTest extends ConteudoApiTest {

    private UUID trilhaId;
    private UUID atividadeId;
    private UUID estudanteId;

    @BeforeEach
    void criarTrilhaEstudada() {
        trilhaId = criarTrilha("Trilha para desativar");
        atividadeId = criarAtividade(criarModulo(trilhaId, "Modulo"), "Atividade");
        estudanteId = criarIntegrante(empresaDeConteudo, "Estudante", emailUnico("estudante"));
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, now())",
                estudanteId,
                atividadeId);
    }

    private boolean listagemTem(UUID id) {
        JsonNode trilhas =
                cliente(tokenNaty).get().uri("/api/v1/trilhas").retrieve().body(JsonNode.class);
        for (JsonNode trilha : trilhas) {
            if (trilha.get("id").asText().equals(id.toString())) {
                return true;
            }
        }
        return false;
    }

    private ResponseEntity<JsonNode> definirAtiva(boolean ativa) {
        return comoNaty(HttpMethod.PUT, "/api/v1/trilhas/" + trilhaId + "/ativa", Map.of("ativa", ativa));
    }

    @Test
    void trilhaDesativadaSaiDaListagemEMantemOProgresso() {
        assertThat(listagemTem(trilhaId)).isTrue();

        ResponseEntity<JsonNode> resposta = definirAtiva(false);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().get("ativa").asBoolean()).isFalse();
        assertThat(listagemTem(trilhaId)).isFalse();
        assertThat(contar("progresso_atividade", "atividade_id", atividadeId)).isEqualTo(1);
    }

    @Test
    void trilhaReativadaVoltaAListagem() {
        definirAtiva(false);

        ResponseEntity<JsonNode> resposta = definirAtiva(true);

        assertThat(resposta.getBody().get("ativa").asBoolean()).isTrue();
        assertThat(listagemTem(trilhaId)).isTrue();
    }

    @Test
    void ativacaoSemOCampoEhErroDeValidacao() {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.PUT, "/api/v1/trilhas/" + trilhaId + "/ativa", Map.of());

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }
}
