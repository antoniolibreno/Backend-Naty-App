package com.projetointegrador.natysync.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.usuario.Papel;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class FormatoDeErroApiTest extends IntegracaoTest {

    private void assertErro(ResponseEntity<JsonNode> resposta, HttpStatus status, String codigo) {
        assertThat(resposta.getStatusCode()).isEqualTo(status);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo(codigo);
        assertThat(resposta.getBody().get("status").asInt()).isEqualTo(status.value());
        assertThat(resposta.getBody().has("caminho")).isTrue();
    }

    @Test
    void rotaInexistenteComTokenEh404() {
        ResponseEntity<JsonNode> resposta =
                cliente().get().uri("/api/v1/rota-inexistente").retrieve().toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.NOT_FOUND, "RECURSO_NAO_ENCONTRADO");
    }

    @Test
    void rotaInexistenteSemTokenContinua401() {
        ResponseEntity<JsonNode> resposta = clienteSemSessao()
                .get()
                .uri("/api/v1/rota-inexistente")
                .retrieve()
                .toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.UNAUTHORIZED, "CREDENCIAL_INVALIDA");
    }

    @Test
    void identificadorMalFormadoNaUrlEh400() {
        ResponseEntity<JsonNode> resposta =
                cliente().get().uri("/api/v1/trilhas/nao-e-uuid").retrieve().toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.BAD_REQUEST, "REQUISICAO_MALFORMADA");
    }

    @Test
    void jsonMalFormadoEh400SemEcoarOCorpo() {
        ResponseEntity<JsonNode> resposta = clienteSemSessao()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"email\":\"alguem@teste.com.br\",\"senha\":\"segredo-que-nao-volta\"")
                .retrieve()
                .toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.BAD_REQUEST, "REQUISICAO_MALFORMADA");
        assertThat(resposta.getBody().toString()).doesNotContain("segredo-que-nao-volta");
    }

    @Test
    void metodoNaoSuportadoEh405() {
        ResponseEntity<JsonNode> resposta =
                cliente().delete().uri("/api/v1/trilhas").retrieve().toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.METHOD_NOT_ALLOWED, "METODO_NAO_SUPORTADO");
    }

    @Test
    void tipoDeConteudoNaoSuportadoEh415() {
        ResponseEntity<JsonNode> resposta = clienteSemSessao()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.TEXT_PLAIN)
                .body("email=alguem")
                .retrieve()
                .toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_DE_CONTEUDO_NAO_SUPORTADO");
    }

    @Test
    void despachoDeErroNaoViraRotaPublica() {
        ResponseEntity<JsonNode> resposta =
                clienteSemSessao().get().uri("/error").retrieve().toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.UNAUTHORIZED, "CREDENCIAL_INVALIDA");
    }

    @Test
    void parametroDeTipoErradoEh400() {
        String tokenAdmin = criarContaAdmin();

        ResponseEntity<JsonNode> resposta = cliente(tokenAdmin)
                .get()
                .uri("/api/v1/painel/integrantes?ativo=talvez")
                .retrieve()
                .toEntity(JsonNode.class);

        assertErro(resposta, HttpStatus.BAD_REQUEST, "REQUISICAO_MALFORMADA");
    }

    private String criarContaAdmin() {
        String email = "admin-formato-" + java.util.UUID.randomUUID() + "@teste.com.br";
        criarIntegrante(criarEmpresa("Empresa do Formato"), "Admin", email, Papel.ADMIN);
        return autenticar(email, SENHA_DE_TESTE);
    }
}
