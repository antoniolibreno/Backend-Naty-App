package com.projetointegrador.natysync.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class SessaoApiTest extends IntegracaoTest {

    private UUID empresaId;
    private UUID usuarioId;

    @BeforeEach
    void criarIntegrante() {
        Empresa empresa = criarEmpresa("Empresa de Teste");
        empresaId = empresa.getId();
        usuarioId = criarIntegrante(empresa, "Integrante de Teste", "integrante@teste.com.br");
    }

    @AfterEach
    void limpar() {
        usuarioRepository.deleteById(usuarioId);
        empresaRepository.deleteById(empresaId);
    }

    private <T> ResponseEntity<T> criarSessao(Object corpo, Class<T> tipo) {
        return clienteSemSessao()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .toEntity(tipo);
    }

    @Test
    void autenticaEDevolveIntegranteEmpresaEToken() {
        ResponseEntity<JsonNode> resposta =
                criarSessao(Map.of("email", "integrante@teste.com.br", "senha", SENHA_DE_TESTE), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("usuarioId").asText()).isEqualTo(usuarioId.toString());
        assertThat(resposta.getBody().get("empresaId").asText()).isEqualTo(empresaId.toString());
        assertThat(resposta.getBody().get("nome").asText()).isEqualTo("Integrante de Teste");
        assertThat(resposta.getBody().get("token").asText()).isNotBlank();
        assertThat(resposta.getBody().get("expiraEm").asText()).isNotBlank();
    }

    @Test
    void resolveComMaiusculasEEspacos() {
        ResponseEntity<JsonNode> resposta =
                criarSessao(Map.of("email", "  Integrante@Teste.COM.BR  ", "senha", SENHA_DE_TESTE), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("usuarioId").asText()).isEqualTo(usuarioId.toString());
    }

    @Test
    void emailInvalidoDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta =
                criarSessao(Map.of("email", "nao-e-email", "senha", SENHA_DE_TESTE), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
    }

    @Test
    void emailAusenteDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = criarSessao(Map.of(), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void senhaAusenteDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = criarSessao(Map.of("email", "integrante@teste.com.br"), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
    }

    @Test
    void senhaVaziaDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta =
                criarSessao(Map.of("email", "integrante@teste.com.br", "senha", "  "), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void senhaNaoRetornaNaResposta() {
        ResponseEntity<String> resposta =
                criarSessao(Map.of("email", "integrante@teste.com.br", "senha", SENHA_DE_TESTE), String.class);

        assertThat(resposta.getBody()).doesNotContain(SENHA_DE_TESTE);
        assertThat(resposta.getBody()).doesNotContain("senha");
    }
}
