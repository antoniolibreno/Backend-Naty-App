package com.projetointegrador.natysync.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class SessaoApiTest extends IntegracaoTest {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UUID empresaId;
    private UUID usuarioId;

    @BeforeEach
    void criarIntegrante() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setNome("Empresa de Teste");
        empresa.setAtiva(true);
        empresa.setCriadoEm(OffsetDateTime.now());
        empresa.setAtualizadoEm(OffsetDateTime.now());
        empresaId = empresaRepository.save(empresa).getId();

        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNatyId("naty-teste-001");
        usuario.setNome("Integrante de Teste");
        usuario.setEmail("integrante@teste.com.br");
        usuario.setPerfil("user");
        usuario.setStatus("offline");
        usuario.setPayload("{}");
        usuarioId = usuarioRepository.save(usuario).getId();
    }

    @AfterEach
    void limpar() {
        usuarioRepository.deleteById(usuarioId);
        empresaRepository.deleteById(empresaId);
    }

    private <T> ResponseEntity<T> criarSessao(Object corpo, Class<T> tipo) {
        return cliente()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo)
                .retrieve()
                .toEntity(tipo);
    }

    @Test
    void resolveIntegrantePorEmail() {
        ResponseEntity<JsonNode> resposta = criarSessao(Map.of("email", "integrante@teste.com.br"), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("usuarioId").asText()).isEqualTo(usuarioId.toString());
        assertThat(resposta.getBody().get("empresaId").asText()).isEqualTo(empresaId.toString());
        assertThat(resposta.getBody().get("nome").asText()).isEqualTo("Integrante de Teste");
    }

    @Test
    void resolveComMaiusculasEEspacos() {
        ResponseEntity<JsonNode> resposta = criarSessao(Map.of("email", "  Integrante@Teste.COM.BR  "), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("usuarioId").asText()).isEqualTo(usuarioId.toString());
    }

    @Test
    void naoEmiteCredencial() {
        ResponseEntity<String> resposta = criarSessao(Map.of("email", "integrante@teste.com.br"), String.class);

        assertThat(resposta.getBody()).doesNotContain("token");
        assertThat(resposta.getHeaders().get("Set-Cookie")).isNull();
    }

    @Test
    void emailInexistenteDevolveNaoEncontrado() {
        ResponseEntity<JsonNode> resposta = criarSessao(Map.of("email", "ninguem@teste.com.br"), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_NAO_ENCONTRADO");
    }

    @Test
    void emailInvalidoDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = criarSessao(Map.of("email", "nao-e-email"), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
    }

    @Test
    void emailAusenteDevolveErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = criarSessao(Map.of(), JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
