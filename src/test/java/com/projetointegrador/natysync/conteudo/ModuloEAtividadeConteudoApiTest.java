package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class ModuloEAtividadeConteudoApiTest extends ConteudoApiTest {

    private UUID trilhaId;
    private UUID moduloId;

    @BeforeEach
    void criarPais() {
        trilhaId = criarTrilha("Trilha de modulos");
        moduloId = criarModulo(trilhaId, "Primeiro");
    }

    private Map<String, Object> atividade(Object... paresChaveValor) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("moduloId", moduloId);
        corpo.put("titulo", "Atividade");
        for (int i = 0; i < paresChaveValor.length; i += 2) {
            corpo.put((String) paresChaveValor[i], paresChaveValor[i + 1]);
        }
        return corpo;
    }

    private ResponseEntity<JsonNode> criar(Map<String, Object> corpo) {
        return comoNaty(HttpMethod.POST, "/api/v1/atividades", corpo);
    }

    private int atividadesDoModulo() {
        return contar("atividade", "modulo_id", moduloId);
    }

    @Test
    void modulosNascemNoFimDaTrilha() {
        UUID segundo = criarModulo(trilhaId, "Segundo");

        assertThat(titulosDosModulos(trilhaId)).containsExactly("Primeiro", "Segundo");
        Integer ordem = jdbc.queryForObject("select ordem from modulo where id = ?", Integer.class, segundo);
        assertThat(ordem).isEqualTo(2);
    }

    @Test
    void moduloEmTrilhaInexistenteEhNaoEncontrado() {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.POST, "/api/v1/modulos", Map.of("trilhaId", UUID.randomUUID(), "titulo", "Orfao"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
    }

    @Test
    void alteraEremoveModulo() {
        ResponseEntity<JsonNode> alteracao = comoNaty(
                HttpMethod.PUT, "/api/v1/modulos/" + moduloId, Map.of("titulo", "Renomeado", "descricao", "d"));
        ResponseEntity<JsonNode> remocao = comoNaty(HttpMethod.DELETE, "/api/v1/modulos/" + moduloId, null);

        assertThat(alteracao.getStatusCode().value()).isEqualTo(200);
        assertThat(alteracao.getBody().get("titulo").asText()).isEqualTo("Renomeado");
        assertThat(remocao.getStatusCode().value()).isEqualTo(204);
        assertThat(contar("modulo", "id", moduloId)).isZero();
    }

    @Test
    void atividadeComVideoDoYoutubeEDuracaoEhLidaPeloIntegrante() {
        ResponseEntity<JsonNode> resposta = criar(atividade("videoUrl", URL_DO_YOUTUBE, "duracaoSegundos", 120));

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        UUID id = UUID.fromString(resposta.getBody().get("id").asText());
        JsonNode leitura = cliente(tokenNaty)
                .get()
                .uri("/api/v1/atividades/{id}", id)
                .retrieve()
                .body(JsonNode.class);
        assertThat(leitura.get("videoUrl").asText()).isEqualTo(URL_DO_YOUTUBE);
        assertThat(leitura.get("duracaoSegundos").asInt()).isEqualTo(120);
    }

    @Test
    void videoForaDoYoutubeEhRecusadoENaoGravaAAtividade() {
        ResponseEntity<JsonNode> resposta =
                criar(atividade("videoUrl", "https://exemplo.com/video.mp4", "duracaoSegundos", 60));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
        assertThat(atividadesDoModulo()).isZero();
    }

    @Test
    void videoSemHttpsEhRecusado() {
        ResponseEntity<JsonNode> resposta =
                criar(atividade("videoUrl", "http://www.youtube.com/watch?v=abcdefghijk", "duracaoSegundos", 60));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void videoSemDuracaoPositivaEhRecusado() {
        assertThat(criar(atividade("videoUrl", URL_DO_YOUTUBE)).getStatusCode().value())
                .isEqualTo(400);
        assertThat(criar(atividade("videoUrl", URL_DO_YOUTUBE, "duracaoSegundos", 0))
                        .getStatusCode()
                        .value())
                .isEqualTo(400);
        assertThat(atividadesDoModulo()).isZero();
    }

    @Test
    void duracaoSemVideoEhRecusada() {
        ResponseEntity<JsonNode> resposta = criar(atividade("duracaoSegundos", 60));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
    }

    @Test
    void xpOmitidoVale10EXpNegativoEhRecusado() {
        ResponseEntity<JsonNode> padrao = criar(atividade());
        ResponseEntity<JsonNode> negativo = criar(atividade("xp", -1));

        assertThat(padrao.getBody().get("xp").asInt()).isEqualTo(10);
        assertThat(negativo.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void atividadeEmModuloInexistenteEhNaoEncontrada() {
        ResponseEntity<JsonNode> resposta = criar(Map.of("moduloId", UUID.randomUUID(), "titulo", "Orfa"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void alteraAtividadeEValidaOVideoNaAlteracao() {
        UUID id = criarAtividade(moduloId, "Antes");

        ResponseEntity<JsonNode> alteracao = comoNaty(
                HttpMethod.PUT,
                "/api/v1/atividades/" + id,
                Map.of("titulo", "Depois", "xp", 25, "videoUrl", URL_DO_YOUTUBE, "duracaoSegundos", 90));
        ResponseEntity<JsonNode> invalida = comoNaty(
                HttpMethod.PUT,
                "/api/v1/atividades/" + id,
                Map.of("titulo", "Depois", "videoUrl", "https://exemplo.com/x", "duracaoSegundos", 90));

        assertThat(alteracao.getStatusCode().value()).isEqualTo(200);
        assertThat(alteracao.getBody().get("xp").asInt()).isEqualTo(25);
        assertThat(invalida.getStatusCode().value()).isEqualTo(400);
        assertThat(jdbc.queryForObject("select xp from atividade where id = ?", Integer.class, id))
                .isEqualTo(25);
    }

    @Test
    void removeAtividadeEOModuloLevaAsAtividadesJunto() {
        UUID id = criarAtividade(moduloId, "Sai sozinha");
        criarAtividade(moduloId, "Sai com o modulo");

        ResponseEntity<JsonNode> remocao = comoNaty(HttpMethod.DELETE, "/api/v1/atividades/" + id, null);
        assertThat(remocao.getStatusCode().value()).isEqualTo(204);
        assertThat(atividadesDoModulo()).isEqualTo(1);

        comoNaty(HttpMethod.DELETE, "/api/v1/modulos/" + moduloId, null);
        assertThat(atividadesDoModulo()).isZero();
    }
}
