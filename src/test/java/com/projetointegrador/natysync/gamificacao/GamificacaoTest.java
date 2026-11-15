package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

abstract class GamificacaoTest extends IntegracaoTest {

    static final List<UUID> ATIVIDADES_DA_TRILHA = List.of(
            UUID.fromString("00000000-0000-0000-0002-000000000001"),
            UUID.fromString("00000000-0000-0000-0002-000000000002"),
            UUID.fromString("00000000-0000-0000-0002-000000000003"),
            UUID.fromString("00000000-0000-0000-0002-000000000004"),
            UUID.fromString("00000000-0000-0000-0002-000000000005"),
            UUID.fromString("00000000-0000-0000-0002-000000000006"));

    record Conta(UUID id, String token) {}

    Conta novaConta(Empresa empresa, String nome) {
        return novaConta(empresa, nome, Papel.INTEGRANTE);
    }

    Conta novaConta(Empresa empresa, String nome, Papel papel) {
        String email = "gamificacao-" + UUID.randomUUID() + "@teste.com.br";
        UUID id = criarIntegrante(empresa, nome, email, papel);
        return new Conta(id, autenticar(email, SENHA_DE_TESTE));
    }

    ResponseEntity<JsonNode> aprovar(Conta conta, UUID atividadeId) {
        return enviarTentativa(conta, atividadeId, true);
    }

    ResponseEntity<JsonNode> reprovar(Conta conta, UUID atividadeId) {
        return enviarTentativa(conta, atividadeId, false);
    }

    void concluirAte(Conta conta, int quantidade) {
        for (UUID atividadeId : ATIVIDADES_DA_TRILHA.subList(0, quantidade)) {
            aprovar(conta, atividadeId);
        }
    }

    private ResponseEntity<JsonNode> enviarTentativa(Conta conta, UUID atividadeId, boolean corretas) {
        List<Map<String, String>> respostas = new ArrayList<>();
        List<UUID> perguntas = jdbc.queryForList(
                "select p.id from pergunta p join quiz q on q.id = p.quiz_id where q.atividade_id = ?",
                UUID.class,
                atividadeId);
        for (UUID perguntaId : perguntas) {
            UUID alternativaId = jdbc.queryForObject(
                    "select id from alternativa where pergunta_id = ? and correta = ? order by ordem limit 1",
                    UUID.class,
                    perguntaId,
                    corretas);
            respostas.add(Map.of("perguntaId", perguntaId.toString(), "alternativaId", alternativaId.toString()));
        }
        return cliente(conta.token())
                .post()
                .uri("/api/v1/atividades/{id}/quiz/tentativas", atividadeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("respostas", respostas))
                .retrieve()
                .toEntity(JsonNode.class);
    }

    ResponseEntity<JsonNode> perfil(String token, UUID integranteId) {
        return cliente(token)
                .get()
                .uri("/api/v1/integrantes/{id}/gamificacao", integranteId)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    JsonNode perfil(Conta conta) {
        return perfil(conta.token(), conta.id()).getBody();
    }

    long pontos(Conta conta) {
        return perfil(conta).get("pontos").asLong();
    }

    JsonNode conquista(Conta conta, String codigo) {
        for (JsonNode conquista : perfil(conta).get("conquistas")) {
            if (conquista.get("codigo").asText().equals(codigo)) {
                return conquista;
            }
        }
        throw new AssertionError("Conquista fora do catalogo: " + codigo);
    }

    boolean obteve(Conta conta, String codigo) {
        return !conquista(conta, codigo).get("obtidaEm").isNull();
    }

    List<LocalDate> diasDeEstudo(Conta conta) {
        return jdbc.queryForList(
                "select dia from dia_de_estudo where usuario_id = ? order by dia", LocalDate.class, conta.id());
    }

    int pontosConcedidos(Conta conta) {
        return jdbc.queryForObject(
                "select count(*) from ponto_concedido where usuario_id = ?", Integer.class, conta.id());
    }
}
