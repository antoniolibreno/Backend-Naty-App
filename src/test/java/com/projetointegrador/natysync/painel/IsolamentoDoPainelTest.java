package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class IsolamentoDoPainelTest extends PainelTest {

    private static final String INTEGRANTES = "/api/v1/painel/integrantes";

    private Conta adminA;
    private Conta integranteB;

    @BeforeEach
    void criarDuasEmpresas() {
        adminA = criarConta(criarEmpresa("Empresa A do Painel"), Papel.ADMIN);
        integranteB = criarConta(criarEmpresa("Empresa B do Painel"), Papel.INTEGRANTE);
    }

    private HttpStatus statusDe(HttpMethod metodo, String uri, Object corpo) {
        return (HttpStatus) chamar(adminA.token(), metodo, uri, corpo).getStatusCode();
    }

    @Test
    void adminDeUmaEmpresaNaoAlcancaIntegranteDeOutra() {
        String caminho = INTEGRANTES + "/" + integranteB.id();

        assertThat(statusDe(HttpMethod.GET, caminho, null)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(statusDe(
                        HttpMethod.PUT,
                        caminho,
                        Map.of("nome", "Invadido", "email", integranteB.email(), "papel", "ADMIN")))
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(statusDe(HttpMethod.PUT, caminho + "/senha", Map.of("senha", "senha-invasora")))
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(statusDe(HttpMethod.PUT, caminho + "/ativo", Map.of("ativo", false)))
                .isEqualTo(HttpStatus.NOT_FOUND);

        Usuario intacto = usuarioRepository.findById(integranteB.id()).orElseThrow();
        assertThat(intacto.getNome()).isEqualTo("Conta INTEGRANTE");
        assertThat(intacto.getPapel()).isEqualTo(Papel.INTEGRANTE);
        assertThat(intacto.isAtivo()).isTrue();
        assertThat(autenticarCom(integranteB.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(lerTrilhas(integranteB.token()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listagemDoAdminSoTrazAPropriaEmpresa() {
        JsonNode pagina = chamar(adminA.token(), HttpMethod.GET, INTEGRANTES + "?tamanho=100")
                .getBody();

        assertThat(pagina.get("totalItens").asLong()).isEqualTo(1);
        assertThat(pagina.get("itens").get(0).get("id").asText())
                .isEqualTo(adminA.id().toString());
        assertThat(pagina.toString()).doesNotContain(integranteB.email());
    }

    @Test
    void adminNaoCadastraIntegranteEmOutraEmpresa() {
        ResponseEntity<JsonNode> resposta = chamar(
                adminA.token(),
                HttpMethod.POST,
                "/api/v1/painel/empresas/" + integranteB.empresaId() + "/integrantes",
                novoIntegrante("invasor-" + java.util.UUID.randomUUID() + "@teste.com.br", Papel.INTEGRANTE));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(jdbc.queryForObject(
                        "select count(*) from usuario where empresa_id = ?", Long.class, integranteB.empresaId()))
                .isEqualTo(1L);
    }
}
