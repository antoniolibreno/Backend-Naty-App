package com.projetointegrador.natysync.acompanhamento;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AutorizacaoDeAcompanhamentoTest extends IntegracaoTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000cc");
    private static final List<String> ROTAS = List.of(
            "/api/v1/acompanhamento/empresas/" + ID + "/integrantes",
            "/api/v1/acompanhamento/empresas/" + ID + "/metricas",
            "/api/v1/acompanhamento/empresas/" + ID + "/integrantes.csv",
            "/api/v1/acompanhamento/atividades");

    private String tokenAdmin;
    private String tokenIntegrante;

    @BeforeEach
    void criarContas() {
        Empresa empresa = criarEmpresa("Empresa Autorizacao de Acompanhamento");
        String emailAdmin = emailUnico("admin");
        String emailIntegrante = emailUnico("integrante");
        criarIntegrante(empresa, "Administrador", emailAdmin, Papel.ADMIN);
        criarIntegrante(empresa, "Integrante", emailIntegrante);
        tokenAdmin = autenticar(emailAdmin, SENHA_DE_TESTE);
        tokenIntegrante = autenticar(emailIntegrante, SENHA_DE_TESTE);
    }

    private ResponseEntity<JsonNode> chamar(String rota, String token) {
        var cliente = token == null ? clienteSemSessao() : cliente(token);
        return cliente.get().uri(rota).retrieve().toEntity(JsonNode.class);
    }

    @Test
    void adminEIntegranteRecebemAcessoNegadoEmTodaRota() {
        for (String rota : ROTAS) {
            for (String token : List.of(tokenAdmin, tokenIntegrante)) {
                ResponseEntity<JsonNode> resposta = chamar(rota, token);

                assertThat(resposta.getStatusCode().value()).as(rota).isEqualTo(403);
                assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
            }
        }
    }

    @Test
    void semTokenRecebeCredencialInvalidaEmTodaRota() {
        for (String rota : ROTAS) {
            ResponseEntity<JsonNode> resposta = chamar(rota, null);

            assertThat(resposta.getStatusCode().value()).as(rota).isEqualTo(401);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
        }
    }
}
