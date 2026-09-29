package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class EmpresaPainelApiTest extends PainelTest {

    private Conta naty;

    @BeforeEach
    void criarNaty() {
        naty = criarConta(criarEmpresa("Naty de Teste"), Papel.NATY);
    }

    private ResponseEntity<JsonNode> criarPelaApi(String nome, String fuso) {
        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.POST,
                "/api/v1/painel/empresas",
                Map.of("nome", nome, "ativa", true, "fusoHorario", fuso));
        if (resposta.getStatusCode() == HttpStatus.CREATED) {
            registrarEmpresaCriada(UUID.fromString(resposta.getBody().get("id").asText()));
        }
        return resposta;
    }

    @Test
    void natyCriaELeEmpresaComFuso() {
        ResponseEntity<JsonNode> criada = criarPelaApi("Cliente Novo", "America/Manaus");

        assertThat(criada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode corpo = criada.getBody();
        assertThat(corpo.get("fusoHorario").asText()).isEqualTo("America/Manaus");
        assertThat(corpo.get("criadoEm").isNull()).isFalse();

        ResponseEntity<JsonNode> lida = chamar(
                naty.token(),
                HttpMethod.GET,
                "/api/v1/painel/empresas/" + corpo.get("id").asText());
        assertThat(lida.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lida.getBody().get("nome").asText()).isEqualTo("Cliente Novo");
    }

    @Test
    void fusoDesconhecidoEhErroDeValidacao() {
        ResponseEntity<JsonNode> resposta = criarPelaApi("Cliente", "Lua/Crateras");

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("FALHA_DE_VALIDACAO");
    }

    @Test
    void listagemEhPaginada() {
        criarPelaApi("Cliente A", "America/Sao_Paulo");
        criarPelaApi("Cliente B", "America/Sao_Paulo");

        JsonNode pagina = chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/empresas?pagina=0&tamanho=1")
                .getBody();

        assertThat(pagina.get("itens").size()).isEqualTo(1);
        assertThat(pagina.get("tamanho").asInt()).isEqualTo(1);
        assertThat(pagina.get("totalItens").asLong()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void excluirEmpresaComIntegranteEhConflito() {
        Empresa empresa = criarEmpresa("Com Integrante");
        criarIntegrante(empresa, "Alguem", "alguem-" + UUID.randomUUID() + "@teste.com.br");

        ResponseEntity<JsonNode> resposta =
                chamar(naty.token(), HttpMethod.DELETE, "/api/v1/painel/empresas/" + empresa.getId());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("EMPRESA_COM_VINCULOS");
        assertThat(empresaRepository.existsById(empresa.getId())).isTrue();
    }

    @Test
    void excluirEmpresaSemIntegrante() {
        String id =
                criarPelaApi("Vazia", "America/Sao_Paulo").getBody().get("id").asText();

        assertThat(chamar(naty.token(), HttpMethod.DELETE, "/api/v1/painel/empresas/" + id)
                        .getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(empresaRepository.existsById(UUID.fromString(id))).isFalse();
    }

    @Test
    void desativarEmpresaDerrubaSessaoELoginDosIntegrantes() {
        Empresa empresa = criarEmpresa("Cliente que Sai");
        Conta integrante = criarConta(empresa, Papel.INTEGRANTE);
        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<JsonNode> resposta = chamar(
                naty.token(),
                HttpMethod.PUT,
                "/api/v1/painel/empresas/" + empresa.getId(),
                Map.of("nome", "Cliente que Sai", "ativa", false, "fusoHorario", "America/Sao_Paulo"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lerTrilhas(integrante.token()).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(autenticarCom(integrante.email(), SENHA_DE_TESTE).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void natyNaoDesativaNemExcluiAPropriaEmpresa() {
        String caminho = "/api/v1/painel/empresas/" + naty.empresaId();

        ResponseEntity<JsonNode> desativar = chamar(
                naty.token(),
                HttpMethod.PUT,
                caminho,
                Map.of("nome", "Naty de Teste", "ativa", false, "fusoHorario", "America/Sao_Paulo"));
        ResponseEntity<JsonNode> excluir = chamar(naty.token(), HttpMethod.DELETE, caminho);

        assertThat(desativar.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(desativar.getBody().get("codigo").asText()).isEqualTo("OPERACAO_NA_PROPRIA_CONTA");
        assertThat(excluir.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void empresaInexistenteEh404() {
        assertThat(chamar(naty.token(), HttpMethod.GET, "/api/v1/painel/empresas/" + UUID.randomUUID())
                        .getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
