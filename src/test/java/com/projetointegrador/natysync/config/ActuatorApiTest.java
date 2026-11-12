package com.projetointegrador.natysync.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class ActuatorApiTest extends IntegracaoTest {

    private String tokenNaty;
    private String tokenAdmin;
    private String tokenIntegrante;

    @BeforeEach
    void criarContas() {
        Empresa empresa = criarEmpresa("Empresa Actuator");
        tokenNaty = tokenDe(empresa, Papel.NATY);
        tokenAdmin = tokenDe(empresa, Papel.ADMIN);
        tokenIntegrante = tokenDe(empresa, Papel.INTEGRANTE);
    }

    private String tokenDe(Empresa empresa, Papel papel) {
        String email = emailUnico(papel.name().toLowerCase());
        criarIntegrante(empresa, "Conta " + papel, email, papel);
        return autenticar(email, SENHA_DE_TESTE);
    }

    private ResponseEntity<JsonNode> consultar(String token, String uri) {
        var cliente = token == null ? clienteSemSessao() : cliente(token);
        return cliente.get().uri(uri).retrieve().toEntity(JsonNode.class);
    }

    @Test
    void metricasParaNatyListamRequisicaoEBanco() {
        ResponseEntity<JsonNode> resposta = consultar(tokenNaty, "/actuator/metrics");

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().get("names").toString())
                .contains("http.server.requests")
                .contains("hikaricp.connections");
    }

    @Test
    void metricasNegadasParaAdminEIntegrante() {
        for (String token : new String[] {tokenAdmin, tokenIntegrante}) {
            ResponseEntity<JsonNode> resposta = consultar(token, "/actuator/metrics");

            assertThat(resposta.getStatusCode().value()).isEqualTo(403);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
        }
    }

    @Test
    void metricasSemTokenSaoRecusadas() {
        ResponseEntity<JsonNode> resposta = consultar(null, "/actuator/metrics");

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }

    @Test
    void outrosEndpointsDoActuatorExigemNaty() {
        assertThat(consultar(tokenAdmin, "/actuator/flyway").getStatusCode().value())
                .isEqualTo(403);
        assertThat(consultar(tokenNaty, "/actuator/flyway").getStatusCode().value())
                .isEqualTo(200);
    }

    @Test
    void saudePublicaSaiSemDetalhe() {
        ResponseEntity<JsonNode> resposta = consultar(null, "/actuator/health");

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().get("status").asText()).isEqualTo("UP");
        assertThat(resposta.getBody().has("components")).isFalse();
    }

    @Test
    void saudeDetalhadaSoParaNaty() {
        assertThat(consultar(tokenNaty, "/actuator/health").getBody().has("components"))
                .isTrue();
        assertThat(consultar(tokenIntegrante, "/actuator/health").getBody().has("components"))
                .isFalse();
        assertThat(consultar(tokenAdmin, "/actuator/health").getBody().has("components"))
                .isFalse();
    }
}
