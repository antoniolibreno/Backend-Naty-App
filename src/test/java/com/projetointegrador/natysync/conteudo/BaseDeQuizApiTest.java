package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.empresa.Empresa;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

abstract class BaseDeQuizApiTest extends ConteudoApiTest {

    protected UUID trilhaId;
    protected UUID moduloId;
    protected UUID atividadeId;
    protected UUID estudanteId;

    protected void prepararAtividade() {
        trilhaId = criarTrilha("Trilha de quiz");
        moduloId = criarModulo(trilhaId, "Modulo");
        atividadeId = criarAtividade(moduloId, "Atividade");
        Empresa empresa = empresaDeConteudo;
        estudanteId = criarIntegrante(empresa, "Estudante", emailUnico("estudante"));
    }

    protected Map<String, Object> alternativa(String texto, boolean correta) {
        return Map.of("texto", texto, "correta", correta);
    }

    protected Map<String, Object> pergunta(String enunciado, int total, int indiceDaCorreta) {
        List<Map<String, Object>> alternativas = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            alternativas.add(alternativa("Alternativa " + (i + 1), i == indiceDaCorreta));
        }
        return Map.of("enunciado", enunciado, "alternativas", alternativas);
    }

    protected Map<String, Object> corpoDoQuiz(UUID atividade, Integer notaMinima, Map<String, Object>... perguntas) {
        Map<String, Object> corpo = new java.util.HashMap<>();
        corpo.put("atividadeId", atividade);
        if (notaMinima != null) {
            corpo.put("notaMinima", notaMinima);
        }
        corpo.put("perguntas", List.of(perguntas));
        return corpo;
    }

    @SafeVarargs
    protected final JsonNode criarQuiz(Map<String, Object>... perguntas) {
        ResponseEntity<JsonNode> resposta =
                comoNaty(HttpMethod.POST, "/api/v1/quizzes", corpoDoQuiz(atividadeId, null, perguntas));
        return resposta.getBody();
    }

    protected JsonNode lerQuizAdmin(UUID quizId) {
        return comoNaty(HttpMethod.GET, "/api/v1/quizzes/" + quizId, null).getBody();
    }

    protected int corretasDaPergunta(UUID perguntaId) {
        Integer total = jdbc.queryForObject(
                "select count(*) from alternativa where pergunta_id = ? and correta", Integer.class, perguntaId);
        return total == null ? 0 : total;
    }

    protected UUID id(JsonNode no) {
        return UUID.fromString(no.get("id").asText());
    }

    protected void registrarTentativa(UUID quizId, UUID perguntaId, UUID alternativaId) {
        UUID tentativaId = UUID.randomUUID();
        jdbc.update(
                "insert into tentativa_quiz (id, usuario_id, quiz_id, nota, aprovado) values (?, ?, ?, 50, false)",
                tentativaId,
                estudanteId,
                quizId);
        jdbc.update(
                "insert into resposta_tentativa (tentativa_id, pergunta_id, alternativa_id, correta)"
                        + " values (?, ?, ?, false)",
                tentativaId,
                perguntaId,
                alternativaId);
    }
}
