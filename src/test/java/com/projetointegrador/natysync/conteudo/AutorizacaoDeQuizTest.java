package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AutorizacaoDeQuizTest extends IntegracaoTest {

    private record Rota(HttpMethod metodo, String caminho) {}

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000bb");

    private static final List<Rota> ROTAS = List.of(
            new Rota(HttpMethod.GET, "/api/v1/quizzes/" + ID),
            new Rota(HttpMethod.POST, "/api/v1/quizzes"),
            new Rota(HttpMethod.PUT, "/api/v1/quizzes/" + ID),
            new Rota(HttpMethod.DELETE, "/api/v1/quizzes/" + ID),
            new Rota(HttpMethod.POST, "/api/v1/perguntas"),
            new Rota(HttpMethod.PUT, "/api/v1/perguntas/" + ID),
            new Rota(HttpMethod.DELETE, "/api/v1/perguntas/" + ID),
            new Rota(HttpMethod.POST, "/api/v1/alternativas"),
            new Rota(HttpMethod.PUT, "/api/v1/alternativas/" + ID),
            new Rota(HttpMethod.DELETE, "/api/v1/alternativas/" + ID));

    private String tokenAdmin;
    private String tokenIntegrante;

    @BeforeEach
    void criarContas() {
        Empresa empresa = criarEmpresa("Empresa Autorizacao de Quiz");
        String emailAdmin = emailUnico("admin");
        String emailIntegrante = emailUnico("integrante");
        criarIntegrante(empresa, "Administrador", emailAdmin, Papel.ADMIN);
        criarIntegrante(empresa, "Integrante", emailIntegrante);
        tokenAdmin = autenticar(emailAdmin, SENHA_DE_TESTE);
        tokenIntegrante = autenticar(emailIntegrante, SENHA_DE_TESTE);
    }

    private ResponseEntity<JsonNode> chamar(Rota rota, String token) {
        var cliente = token == null ? clienteSemSessao() : cliente(token);
        return cliente.method(rota.metodo())
                .uri(rota.caminho())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @Test
    void adminEIntegranteRecebemAcessoNegadoEmTodaRotaDeQuiz() {
        for (Rota rota : ROTAS) {
            for (String token : List.of(tokenAdmin, tokenIntegrante)) {
                ResponseEntity<JsonNode> resposta = chamar(rota, token);

                assertThat(resposta.getStatusCode().value()).as(rota.toString()).isEqualTo(403);
                assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
            }
        }
    }

    @Test
    void semTokenRecebeCredencialInvalidaEmTodaRotaDeQuiz() {
        for (Rota rota : ROTAS) {
            ResponseEntity<JsonNode> resposta = chamar(rota, null);

            assertThat(resposta.getStatusCode().value()).as(rota.toString()).isEqualTo(401);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
        }
    }
}
