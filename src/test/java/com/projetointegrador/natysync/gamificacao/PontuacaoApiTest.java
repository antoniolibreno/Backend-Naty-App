package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class PontuacaoApiTest extends GamificacaoTest {

    private static final UUID PRIMEIRA = ATIVIDADES_DA_TRILHA.getFirst();
    private Conta conta;
    private UUID trilhaId;
    private UUID moduloId;
    private UUID atividadeDeVideoId;

    @BeforeEach
    void preparar() {
        conta = novaConta(criarEmpresa("Empresa pontuacao"), "Integrante Pontuacao");
        trilhaId = UUID.randomUUID();
        moduloId = UUID.randomUUID();
        atividadeDeVideoId = UUID.randomUUID();
        jdbc.update("insert into trilha (id, titulo, ordem) values (?, 'Trilha Pontuacao', 98)", trilhaId);
        jdbc.update(
                "insert into modulo (id, trilha_id, titulo, ordem) values (?, ?, 'Modulo Pontuacao', 1)",
                moduloId,
                trilhaId);
        jdbc.update(
                "insert into atividade (id, modulo_id, titulo, ordem, video_url, duracao_segundos, xp)"
                        + " values (?, ?, 'Video sem quiz', 1, 'https://youtu.be/abcdefghijk', 120, 15)",
                atividadeDeVideoId,
                moduloId);
    }

    @AfterEach
    void limparConteudo() {
        jdbc.update("delete from ponto_concedido where atividade_id = ?", atividadeDeVideoId);
        jdbc.update("delete from progresso_atividade where atividade_id = ?", atividadeDeVideoId);
        jdbc.update("delete from atividade where modulo_id = ?", moduloId);
        jdbc.update("delete from modulo where id = ?", moduloId);
        jdbc.update("delete from trilha where id = ?", trilhaId);
    }

    @Test
    void conclusaoPorTentativaAprovadaConcedeOXpDaAtividade() {
        assertThat(aprovar(conta, PRIMEIRA).getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(pontos(conta)).isEqualTo(xpDe(PRIMEIRA));
        assertThat(jdbc.queryForObject(
                        "select concedido_em = (select concluido_em from progresso_atividade"
                                + " where usuario_id = ? and atividade_id = ?)"
                                + " from ponto_concedido where usuario_id = ? and atividade_id = ?",
                        Boolean.class,
                        conta.id(),
                        PRIMEIRA,
                        conta.id(),
                        PRIMEIRA))
                .isTrue();
    }

    @Test
    void conclusaoPorVideoEmAtividadeSemQuizConcedeOXp() {
        assertThat(postar("video-iniciado").getStatusCode()).isEqualTo(HttpStatus.OK);
        decorrerDuracao();

        var assistido = postar("video-assistido");

        assertThat(assistido.getBody().get("concluidoEm").isNull()).isFalse();
        assertThat(pontos(conta)).isEqualTo(15);
    }

    @Test
    void videoEmAtividadeComQuizTentativaReprovadaEInicioDeVideoNaoPontuam() {
        cliente(conta.token())
                .post()
                .uri("/api/v1/atividades/{id}/video-assistido", PRIMEIRA)
                .retrieve()
                .toBodilessEntity();
        reprovar(conta, PRIMEIRA);
        postar("video-iniciado");

        assertThat(pontos(conta)).isZero();
        assertThat(pontosConcedidos(conta)).isZero();
    }

    @Test
    void quizRefeitoDepoisDaAprovacaoNaoSomaDeNovo() {
        aprovar(conta, PRIMEIRA);
        aprovar(conta, PRIMEIRA);
        reprovar(conta, PRIMEIRA);

        assertThat(pontos(conta)).isEqualTo(xpDe(PRIMEIRA));
        assertThat(pontosConcedidos(conta)).isEqualTo(1);
    }

    @Test
    void videoAssistidoRepetidoNaoSomaDeNovo() {
        postar("video-iniciado");
        decorrerDuracao();
        postar("video-assistido");
        postar("video-assistido");

        assertThat(pontos(conta)).isEqualTo(15);
        assertThat(pontosConcedidos(conta)).isEqualTo(1);
    }

    @Test
    void tentativasAprovadasConcorrentesConcedemUmaUnicaVez() {
        CountDownLatch largada = new CountDownLatch(1);
        List<CompletableFuture<ResponseEntity<JsonNode>>> chamadas = List.of(
                CompletableFuture.supplyAsync(() -> aprovarNaLargada(largada)),
                CompletableFuture.supplyAsync(() -> aprovarNaLargada(largada)));
        largada.countDown();

        chamadas.forEach(chamada -> assertThat(chamada.join().getStatusCode()).isEqualTo(HttpStatus.OK));
        assertThat(pontos(conta)).isEqualTo(xpDe(PRIMEIRA));
        assertThat(pontosConcedidos(conta)).isEqualTo(1);
    }

    @Test
    void mudarOXpDepoisDaConclusaoNaoMudaOPontoConcedido() {
        int xpOriginal = xpDe(PRIMEIRA);
        aprovar(conta, PRIMEIRA);
        try {
            jdbc.update("update atividade set xp = ? where id = ?", xpOriginal + 25, PRIMEIRA);

            assertThat(pontos(conta)).isEqualTo(xpOriginal);
        } finally {
            jdbc.update("update atividade set xp = ? where id = ?", xpOriginal, PRIMEIRA);
        }
    }

    private ResponseEntity<JsonNode> aprovarNaLargada(CountDownLatch largada) {
        try {
            largada.await();
        } catch (InterruptedException interrupcao) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupcao);
        }
        return aprovar(conta, PRIMEIRA);
    }

    private ResponseEntity<JsonNode> postar(String acao) {
        return cliente(conta.token())
                .post()
                .uri("/api/v1/atividades/{id}/{acao}", atividadeDeVideoId, acao)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    private void decorrerDuracao() {
        jdbc.update(
                "update progresso_atividade set video_iniciado_em = now() - interval '120 seconds'"
                        + " where usuario_id = ? and atividade_id = ?",
                conta.id(),
                atividadeDeVideoId);
    }

    private int xpDe(UUID atividadeId) {
        return jdbc.queryForObject("select xp from atividade where id = ?", Integer.class, atividadeId);
    }
}
