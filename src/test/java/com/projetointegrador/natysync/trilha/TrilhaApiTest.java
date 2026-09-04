package com.projetointegrador.natysync.trilha;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class TrilhaApiTest extends IntegracaoTest {

    private static final UUID TRILHA_SEMEADA = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private TrilhaRepository trilhaRepository;

    private UUID trilhaInativaId;

    @AfterEach
    void removerTrilhaInativa() {
        if (trilhaInativaId != null) {
            trilhaRepository.deleteById(trilhaInativaId);
            trilhaInativaId = null;
        }
    }

    private ResponseEntity<JsonNode> obter(String caminho) {
        return cliente().get().uri(caminho).retrieve().toEntity(JsonNode.class);
    }

    @Test
    void listaApenasTrilhaAtiva() {
        ResponseEntity<JsonNode> resposta = obter("/api/v1/trilhas");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody()).hasSize(1);
        assertThat(resposta.getBody().get(0).get("id").asText()).isEqualTo(TRILHA_SEMEADA.toString());
    }

    @Test
    void trilhaInativaNaoAparece() {
        Trilha inativa = new Trilha();
        inativa.setTitulo("Trilha desativada");
        inativa.setOrdem(99);
        inativa.setAtiva(false);
        trilhaInativaId = trilhaRepository.save(inativa).getId();

        ResponseEntity<JsonNode> resposta = obter("/api/v1/trilhas");

        assertThat(resposta.getBody()).hasSize(1);
        assertThat(resposta.getBody().toString()).doesNotContain(trilhaInativaId.toString());
    }

    @Test
    void detalheTrazModulosEAtividadesEmOrdem() {
        ResponseEntity<JsonNode> resposta = obter("/api/v1/trilhas/" + TRILHA_SEMEADA);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode modulos = resposta.getBody().get("modulos");
        assertThat(modulos).hasSize(2);
        assertThat(modulos.get(0).get("ordem").asInt()).isEqualTo(1);
        assertThat(modulos.get(1).get("ordem").asInt()).isEqualTo(2);

        JsonNode atividades = modulos.get(0).get("atividades");
        assertThat(atividades).hasSize(3);
        assertThat(atividades.get(0).get("ordem").asInt()).isEqualTo(1);
        assertThat(atividades.get(2).get("ordem").asInt()).isEqualTo(3);
    }

    @Test
    void trilhaInexistenteDevolveNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = obter("/api/v1/trilhas/" + UUID.randomUUID());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
    }
}
