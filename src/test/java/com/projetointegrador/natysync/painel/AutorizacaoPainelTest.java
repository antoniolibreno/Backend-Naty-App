package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AutorizacaoPainelTest extends PainelTest {

    private Empresa empresa;
    private Conta integrante;
    private Conta admin;
    private Conta naty;

    @BeforeEach
    void criarContas() {
        empresa = criarEmpresa("Empresa de Autorizacao");
        integrante = criarConta(empresa, Papel.INTEGRANTE);
        admin = criarConta(empresa, Papel.ADMIN);
        naty = criarConta(criarEmpresa("Naty de Autorizacao"), Papel.NATY);
    }

    private List<String> rotasDeEmpresa() {
        return List.of(
                "/api/v1/painel/empresas",
                "/api/v1/painel/empresas/" + empresa.getId(),
                "/api/v1/painel/empresas/" + empresa.getId() + "/integrantes");
    }

    private void assertNegado(ResponseEntity<JsonNode> resposta) {
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
    }

    @Test
    void integranteComumNaoAlcancaNenhumaRotaDoPainel() {
        for (String rota : rotasDeEmpresa()) {
            assertNegado(chamar(integrante.token(), HttpMethod.GET, rota));
        }
        assertNegado(chamar(integrante.token(), HttpMethod.GET, "/api/v1/painel/integrantes"));
        assertNegado(chamar(
                integrante.token(),
                HttpMethod.POST,
                "/api/v1/painel/integrantes",
                novoIntegrante("escalada@teste.com.br", Papel.ADMIN)));
        assertNegado(chamar(
                integrante.token(),
                HttpMethod.PUT,
                "/api/v1/painel/integrantes/" + integrante.id() + "/senha",
                Map.of("senha", "senha-nova-123")));
    }

    @Test
    void adminNaoAlcancaRotasDeEmpresa() {
        for (String rota : rotasDeEmpresa()) {
            assertNegado(chamar(admin.token(), HttpMethod.GET, rota));
        }
    }

    @Test
    void escritaDeEmpresaEhNegadaParaAdminEIntegrante() {
        Map<String, Object> corpo = Map.of("nome", "Tentativa", "ativa", false, "fusoHorario", "America/Sao_Paulo");
        String caminho = "/api/v1/painel/empresas/" + empresa.getId();

        for (Conta conta : List.of(admin, integrante)) {
            assertNegado(chamar(conta.token(), HttpMethod.POST, "/api/v1/painel/empresas", corpo));
            assertNegado(chamar(conta.token(), HttpMethod.PUT, caminho, corpo));
            assertNegado(chamar(conta.token(), HttpMethod.DELETE, caminho));
        }
        assertThat(empresaRepository.findById(empresa.getId()).orElseThrow().isAtiva())
                .isTrue();
    }

    @Test
    void natyNaoUsaARotaDoAdmin() {
        assertNegado(chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/integrantes"));
    }

    @Test
    void rotaDesconhecidaDoPainelEhNegada() {
        assertNegado(chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/qualquer"));
    }

    @Test
    void semTokenEh401() {
        ResponseEntity<JsonNode> resposta = clienteSemSessao()
                .get()
                .uri("/api/v1/painel/integrantes")
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
    }
}
