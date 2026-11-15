package com.projetointegrador.natysync.conteudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class RemocaoDeConteudoApiTest extends ConteudoApiTest {

    private UUID trilhaId;
    private UUID moduloId;
    private UUID atividadeId;
    private UUID estudanteId;

    @BeforeEach
    void criarConteudo() {
        trilhaId = criarTrilha("Trilha com historico");
        moduloId = criarModulo(trilhaId, "Modulo");
        atividadeId = criarAtividade(moduloId, "Atividade");
        estudanteId = criarIntegrante(empresaDeConteudo, "Estudante", emailUnico("estudante"));
    }

    private void registrarProgresso() {
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, now())",
                estudanteId,
                atividadeId);
    }

    private void registrarPonto() {
        jdbc.update(
                "insert into ponto_concedido (usuario_id, atividade_id, xp, concedido_em) values (?, ?, 10, now())",
                estudanteId,
                atividadeId);
    }

    private void registrarTentativa() {
        UUID quizId = UUID.randomUUID();
        jdbc.update("insert into quiz (id, atividade_id) values (?, ?)", quizId, atividadeId);
        jdbc.update(
                "insert into tentativa_quiz (usuario_id, quiz_id, nota, aprovado) values (?, ?, 50, false)",
                estudanteId,
                quizId);
    }

    private void exigirRecusaEConteudoIntacto(ResponseEntity<JsonNode> resposta) {
        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CONTEUDO_COM_PROGRESSO");
        assertThat(contar("atividade", "id", atividadeId)).isEqualTo(1);
        assertThat(contar("modulo", "id", moduloId)).isEqualTo(1);
        assertThat(contar("trilha", "id", trilhaId)).isEqualTo(1);
    }

    @Test
    void atividadeComProgressoNaoEhRemovida() {
        registrarProgresso();

        exigirRecusaEConteudoIntacto(comoNaty(HttpMethod.DELETE, "/api/v1/atividades/" + atividadeId, null));
    }

    @Test
    void atividadeComPontoNaoEhRemovida() {
        registrarPonto();

        exigirRecusaEConteudoIntacto(comoNaty(HttpMethod.DELETE, "/api/v1/atividades/" + atividadeId, null));
    }

    @Test
    void atividadeComTentativaDeQuizNaoEhRemovida() {
        registrarTentativa();

        exigirRecusaEConteudoIntacto(comoNaty(HttpMethod.DELETE, "/api/v1/atividades/" + atividadeId, null));
    }

    @Test
    void moduloEtrilhaComAtividadeEstudadaNaoSaoRemovidos() {
        registrarProgresso();

        exigirRecusaEConteudoIntacto(comoNaty(HttpMethod.DELETE, "/api/v1/modulos/" + moduloId, null));
        exigirRecusaEConteudoIntacto(comoNaty(HttpMethod.DELETE, "/api/v1/trilhas/" + trilhaId, null));
    }

    @Test
    void mensagemIndicaADesativacaoComoAlternativa() {
        registrarProgresso();

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/trilhas/" + trilhaId, null);

        assertThat(resposta.getBody().get("mensagem").asText()).containsIgnoringCase("desative");
    }

    @Test
    void conteudoSemHistoricoEhRemovido() {
        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/trilhas/" + trilhaId, null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
        assertThat(contar("atividade", "id", atividadeId)).isZero();
        assertThat(contar("modulo", "id", moduloId)).isZero();
        assertThat(contar("trilha", "id", trilhaId)).isZero();
    }

    @Test
    void historicoDeOutraAtividadeNaoBloqueiaARemocao() {
        UUID outraTrilha = criarTrilha("Outra trilha");
        UUID outraAtividade = criarAtividade(criarModulo(outraTrilha, "M"), "A");
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, now())",
                estudanteId,
                outraAtividade);

        ResponseEntity<JsonNode> resposta = comoNaty(HttpMethod.DELETE, "/api/v1/atividades/" + atividadeId, null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(204);
    }
}
