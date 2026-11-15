package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.usuario.Papel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

abstract class ConteudoApiTest extends IntegracaoTest {

    protected static final UUID TRILHA_SEMEADA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    protected static final String URL_DO_YOUTUBE = "https://www.youtube.com/watch?v=abcdefghijk";

    protected String tokenNaty;
    protected UUID usuarioNatyId;
    protected Empresa empresaDeConteudo;
    private final List<UUID> trilhasCriadas = new ArrayList<>();

    @BeforeEach
    void criarContaNaty() {
        empresaDeConteudo = criarEmpresa("Empresa Conteudo");
        String email = emailUnico("naty");
        usuarioNatyId = criarIntegrante(empresaDeConteudo, "Conta Naty", email, Papel.NATY);
        tokenNaty = autenticar(email, SENHA_DE_TESTE);
    }

    @AfterEach
    void limparConteudoCriado() {
        for (UUID trilhaId : trilhasCriadas) {
            apagarTrilhaPorSql(trilhaId);
        }
        trilhasCriadas.clear();
    }

    protected ResponseEntity<JsonNode> enviar(HttpMethod metodo, String uri, Object corpo, String token) {
        var cliente = token == null ? clienteSemSessao() : cliente(token);
        var requisicao = cliente.method(metodo).uri(uri);
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).body(corpo);
        }
        return requisicao.retrieve().toEntity(JsonNode.class);
    }

    protected ResponseEntity<JsonNode> comoNaty(HttpMethod metodo, String uri, Object corpo) {
        return enviar(metodo, uri, corpo, tokenNaty);
    }

    protected UUID criarTrilha(String titulo) {
        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.POST, "/api/v1/trilhas", Map.of("titulo", titulo));
        UUID id = UUID.fromString(resposta.getBody().get("id").asText());
        trilhasCriadas.add(id);
        return id;
    }

    protected UUID criarModulo(UUID trilhaId, String titulo) {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.POST, "/api/v1/modulos", Map.of("trilhaId", trilhaId, "titulo", titulo));
        return UUID.fromString(resposta.getBody().get("id").asText());
    }

    protected UUID criarAtividade(UUID moduloId, String titulo) {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.POST, "/api/v1/atividades", Map.of("moduloId", moduloId, "titulo", titulo));
        return UUID.fromString(resposta.getBody().get("id").asText());
    }

    protected UUID registrarTrilhaCriadaPorSql(UUID trilhaId) {
        trilhasCriadas.add(trilhaId);
        return trilhaId;
    }

    protected JsonNode lerTrilha(UUID trilhaId) {
        return cliente(tokenNaty)
                .get()
                .uri("/api/v1/trilhas/{id}", trilhaId)
                .retrieve()
                .body(JsonNode.class);
    }

    protected List<String> titulosDosModulos(UUID trilhaId) {
        List<String> titulos = new ArrayList<>();
        lerTrilha(trilhaId)
                .get("modulos")
                .forEach(modulo -> titulos.add(modulo.get("titulo").asText()));
        return titulos;
    }

    protected int contar(String tabela, String colunaDeFiltro, UUID valor) {
        Integer total = jdbc.queryForObject(
                "select count(*) from " + tabela + " where " + colunaDeFiltro + " = ?", Integer.class, valor);
        return total == null ? 0 : total;
    }

    private void apagarTrilhaPorSql(UUID trilhaId) {
        String atividades = "select a.id from atividade a join modulo m on m.id = a.modulo_id where m.trilha_id = ?";
        String quizzes = "select q.id from quiz q where q.atividade_id in (" + atividades + ")";
        String perguntas = "select p.id from pergunta p where p.quiz_id in (" + quizzes + ")";
        jdbc.update("delete from resposta_tentativa where pergunta_id in (" + perguntas + ")", trilhaId);
        jdbc.update("delete from tentativa_quiz where quiz_id in (" + quizzes + ")", trilhaId);
        jdbc.update("delete from ponto_concedido where atividade_id in (" + atividades + ")", trilhaId);
        jdbc.update("delete from progresso_atividade where atividade_id in (" + atividades + ")", trilhaId);
        jdbc.update("delete from alternativa where pergunta_id in (" + perguntas + ")", trilhaId);
        jdbc.update("delete from pergunta where quiz_id in (" + quizzes + ")", trilhaId);
        jdbc.update("delete from quiz where atividade_id in (" + atividades + ")", trilhaId);
        jdbc.update("delete from atividade where modulo_id in (select id from modulo where trilha_id = ?)", trilhaId);
        jdbc.update("delete from modulo where trilha_id = ?", trilhaId);
        jdbc.update("delete from trilha where id = ?", trilhaId);
    }
}
