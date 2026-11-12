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

class AutorizacaoDeConteudoTest extends IntegracaoTest {

    private record Rota(HttpMethod metodo, String caminho) {}

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private static final UUID ATIVIDADE_SEMEADA = UUID.fromString("00000000-0000-0000-0002-000000000001");

    private static final List<Rota> ROTAS_DE_ESCRITA = List.of(
            new Rota(HttpMethod.POST, "/api/v1/trilhas"),
            new Rota(HttpMethod.PUT, "/api/v1/trilhas/" + ID),
            new Rota(HttpMethod.DELETE, "/api/v1/trilhas/" + ID),
            new Rota(HttpMethod.PUT, "/api/v1/trilhas/" + ID + "/ativa"),
            new Rota(HttpMethod.PUT, "/api/v1/trilhas/" + ID + "/modulos/ordem"),
            new Rota(HttpMethod.POST, "/api/v1/modulos"),
            new Rota(HttpMethod.PUT, "/api/v1/modulos/" + ID),
            new Rota(HttpMethod.DELETE, "/api/v1/modulos/" + ID),
            new Rota(HttpMethod.PUT, "/api/v1/modulos/" + ID + "/atividades/ordem"),
            new Rota(HttpMethod.POST, "/api/v1/atividades"),
            new Rota(HttpMethod.PUT, "/api/v1/atividades/" + ID),
            new Rota(HttpMethod.DELETE, "/api/v1/atividades/" + ID),
            new Rota(HttpMethod.GET, "/api/v1/alteracoes-de-conteudo"));

    private String tokenAdmin;
    private String tokenIntegrante;

    @BeforeEach
    void criarContas() {
        Empresa empresa = criarEmpresa("Empresa Autorizacao de Conteudo");
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
    void integranteEAdminRecebemAcessoNegadoEmTodaRotaDeEscrita() {
        for (Rota rota : ROTAS_DE_ESCRITA) {
            for (String token : List.of(tokenAdmin, tokenIntegrante)) {
                ResponseEntity<JsonNode> resposta = chamar(rota, token);

                assertThat(resposta.getStatusCode().value()).as(rota.toString()).isEqualTo(403);
                assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
            }
        }
    }

    @Test
    void semTokenRecebeCredencialInvalidaEmTodaRotaDeEscrita() {
        for (Rota rota : ROTAS_DE_ESCRITA) {
            ResponseEntity<JsonNode> resposta = chamar(rota, null);

            assertThat(resposta.getStatusCode().value()).as(rota.toString()).isEqualTo(401);
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CREDENCIAL_INVALIDA");
        }
    }

    @Test
    void rotasDoIntegranteContinuamAbertasAEle() {
        ResponseEntity<JsonNode> leitura = cliente(tokenIntegrante)
                .get()
                .uri("/api/v1/atividades/{id}", ATIVIDADE_SEMEADA)
                .retrieve()
                .toEntity(JsonNode.class);
        ResponseEntity<JsonNode> video = cliente(tokenIntegrante)
                .post()
                .uri("/api/v1/atividades/{id}/video-assistido", ATIVIDADE_SEMEADA)
                .retrieve()
                .toEntity(JsonNode.class);

        assertThat(leitura.getStatusCode().value()).isEqualTo(200);
        assertThat(video.getStatusCode().value()).isNotIn(401, 403);
    }
}
