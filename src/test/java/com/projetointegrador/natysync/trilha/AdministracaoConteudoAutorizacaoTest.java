package com.projetointegrador.natysync.trilha;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class AdministracaoConteudoAutorizacaoTest extends com.projetointegrador.natysync.IntegracaoTest {

    private String integranteToken;
    private String adminToken;

    @BeforeEach
    void criarContas() {
        Empresa empresa = criarEmpresa("Empresa CRUD conteudo");
        integranteToken = autenticar(criarConta(empresa, Papel.INTEGRANTE), SENHA_DE_TESTE);
        adminToken = autenticar(criarConta(empresa, Papel.ADMIN), SENHA_DE_TESTE);
    }

    private String criarConta(Empresa empresa, Papel papel) {
        String email = papel.name().toLowerCase() + "-" + UUID.randomUUID() + "@teste.com.br";
        criarIntegrante(empresa, "Conta de teste", email, papel);
        return email;
    }

    private ResponseEntity<JsonNode> chamar(String token, HttpMethod metodo, String uri) {
        return cliente(token).method(metodo).uri(uri).retrieve().toEntity(JsonNode.class);
    }

    private List<String> rotasPost() {
        return List.of(
                "/api/v1/trilhas",
                "/api/v1/modulos",
                "/api/v1/atividades",
                "/api/v1/quizzes",
                "/api/v1/perguntas",
                "/api/v1/perguntas/" + UUID.randomUUID() + "/alternativas");
    }

    private List<String> rotasPut() {
        String id = UUID.randomUUID().toString();
        return List.of(
                "/api/v1/trilhas/" + id,
                "/api/v1/trilhas/" + id + "/ativa",
                "/api/v1/modulos/" + id,
                "/api/v1/modulos/ordem",
                "/api/v1/atividades/" + id,
                "/api/v1/atividades/ordem",
                "/api/v1/quizzes/" + id,
                "/api/v1/perguntas/" + id,
                "/api/v1/alternativas/" + id);
    }

    private List<String> rotasDelete() {
        String id = UUID.randomUUID().toString();
        return List.of(
                "/api/v1/trilhas/" + id,
                "/api/v1/modulos/" + id,
                "/api/v1/atividades/" + id,
                "/api/v1/quizzes/" + id,
                "/api/v1/perguntas/" + id,
                "/api/v1/alternativas/" + id);
    }

    private void verificarNegado(ResponseEntity<JsonNode> resposta) {
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
    }

    @Test
    void integranteComumNaoEscreveConteudo() {
        for (String rota : rotasPost()) {
            verificarNegado(chamar(integranteToken, HttpMethod.POST, rota));
        }
        for (String rota : rotasPut()) {
            verificarNegado(chamar(integranteToken, HttpMethod.PUT, rota));
        }
        for (String rota : rotasDelete()) {
            verificarNegado(chamar(integranteToken, HttpMethod.DELETE, rota));
        }
    }

    @Test
    void administradorDeClienteNaoEscreveConteudoGlobal() {
        for (String rota : rotasPost()) {
            verificarNegado(chamar(adminToken, HttpMethod.POST, rota));
        }
        for (String rota : rotasPut()) {
            verificarNegado(chamar(adminToken, HttpMethod.PUT, rota));
        }
        for (String rota : rotasDelete()) {
            verificarNegado(chamar(adminToken, HttpMethod.DELETE, rota));
        }
    }
}
