package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

abstract class PainelTest extends IntegracaoTest {

    record Conta(UUID id, UUID empresaId, String email, String token) {}

    protected Conta criarConta(Empresa empresa, Papel papel) {
        String email = papel.name().toLowerCase() + "-" + UUID.randomUUID() + "@teste.com.br";
        UUID id = criarIntegrante(empresa, "Conta " + papel.name(), email, papel);
        return new Conta(id, empresa.getId(), email, autenticar(email, SENHA_DE_TESTE));
    }

    protected ResponseEntity<JsonNode> chamar(String token, HttpMethod metodo, String uri, Object corpo) {
        var requisicao = cliente(token).method(metodo).uri(uri);
        if (corpo != null) {
            requisicao = requisicao.contentType(MediaType.APPLICATION_JSON).body(corpo);
        }
        return requisicao.retrieve().toEntity(JsonNode.class);
    }

    protected ResponseEntity<JsonNode> chamar(String token, HttpMethod metodo, String uri) {
        return chamar(token, metodo, uri, null);
    }

    protected ResponseEntity<JsonNode> autenticarCom(String email, String senha) {
        return clienteSemSessao()
                .post()
                .uri("/api/v1/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "senha", senha))
                .retrieve()
                .toEntity(JsonNode.class);
    }

    protected ResponseEntity<JsonNode> lerTrilhas(String token) {
        return chamar(token, HttpMethod.GET, "/api/v1/trilhas");
    }

    protected Map<String, Object> novoIntegrante(String email, Papel papel) {
        return Map.of("nome", "Novo Integrante", "email", email, "senha", "senha-nova-123", "papel", papel.name());
    }
}
