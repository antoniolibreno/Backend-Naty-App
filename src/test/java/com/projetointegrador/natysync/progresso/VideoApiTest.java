package com.projetointegrador.natysync.progresso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.projetointegrador.natysync.IntegracaoTest;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

class VideoApiTest extends IntegracaoTest {

    private UUID trilhaId;
    private UUID moduloId;
    private UUID atividadeId;
    private UUID bloqueadaId;
    private UUID integranteId;
    private String token;

    @BeforeEach
    void preparar() {
        var empresa = criarEmpresa("Empresa Video");
        String email = UUID.randomUUID() + "@video.teste";
        integranteId = criarIntegrante(empresa, "Integrante Video", email);
        token = autenticar(email, SENHA_DE_TESTE);
        trilhaId = UUID.randomUUID();
        moduloId = UUID.randomUUID();
        atividadeId = UUID.randomUUID();
        bloqueadaId = UUID.randomUUID();
        jdbc.update("insert into trilha (id, titulo, ordem) values (?, 'Trilha Video', 99)", trilhaId);
        jdbc.update(
                "insert into modulo (id, trilha_id, titulo, ordem) values (?, ?, 'Modulo Video', 1)",
                moduloId,
                trilhaId);
        jdbc.update(
                "insert into atividade (id, modulo_id, titulo, ordem, video_url, duracao_segundos) values (?, ?, 'Video', 1, 'https://youtu.be/abcdefghijk', 120)",
                atividadeId,
                moduloId);
        jdbc.update(
                "insert into atividade (id, modulo_id, titulo, ordem) values (?, ?, 'Sem video', 2)",
                bloqueadaId,
                moduloId);
    }

    @AfterEach
    void limparConteudo() {
        jdbc.update("delete from progresso_atividade where atividade_id in (?, ?)", atividadeId, bloqueadaId);
        jdbc.update("delete from quiz where atividade_id = ?", atividadeId);
        jdbc.update("delete from atividade where modulo_id = ?", moduloId);
        jdbc.update("delete from modulo where id = ?", moduloId);
        jdbc.update("delete from trilha where id = ?", trilhaId);
    }

    private ResponseEntity<JsonNode> chamar(String acao) {
        return cliente(token)
                .post()
                .uri("/api/v1/atividades/{id}/{acao}", atividadeId, acao)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    private void decorrerDuracao() {
        jdbc.update(
                "update progresso_atividade set video_iniciado_em = now() - interval '120 seconds' where usuario_id = ? and atividade_id = ?",
                integranteId,
                atividadeId);
    }

    @Test
    void recusaAssistenciaSemInicio() {
        var resposta = chamar("video-assistido");
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("VIDEO_NAO_INICIADO");
        assertThat(jdbc.queryForObject(
                        "select count(*) from progresso_atividade where usuario_id = ?", Integer.class, integranteId))
                .isZero();
    }

    @Test
    void recusaAssistenciaAntesDaDuracaoSemConcluir() {
        assertThat(chamar("video-iniciado").getStatusCode()).isEqualTo(HttpStatus.OK);
        var resposta = chamar("video-assistido");
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("VIDEO_TEMPO_INSUFICIENTE");
        assertThat(jdbc.queryForObject(
                        "select count(*) from progresso_atividade where usuario_id = ? and (video_assistido_em is not null or concluido_em is not null)",
                        Integer.class,
                        integranteId))
                .isZero();
    }

    @Test
    void inicioRepetidoPreservaCarimboEAssistenciaConcluiSemQuiz() {
        var primeira = chamar("video-iniciado");
        var segunda = chamar("video-iniciado");
        assertThat(segunda.getBody().get("videoIniciadoEm").asText())
                .isEqualTo(primeira.getBody().get("videoIniciadoEm").asText());
        assertThat(segunda.getBody().get("duracaoSegundos").asInt()).isEqualTo(120);
        decorrerDuracao();
        var assistido = chamar("video-assistido");
        assertThat(assistido.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assistido.getBody().get("estado").asText()).isEqualTo("CONCLUIDO");
        assertThat(assistido.getBody().get("proximaAtividadeId").asText()).isEqualTo(bloqueadaId.toString());
        assertThat(chamar("video-assistido").getBody()).isEqualTo(assistido.getBody());
        assertThat(jdbc.queryForObject(
                        "select count(*) from progresso_atividade where usuario_id = ?", Integer.class, integranteId))
                .isEqualTo(1);
    }

    @Test
    void videoComQuizNaoConcluiMesmoDepoisDaDuracao() {
        jdbc.update("insert into quiz (atividade_id) values (?)", atividadeId);
        chamar("video-iniciado");
        decorrerDuracao();
        var assistido = chamar("video-assistido");
        assertThat(assistido.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assistido.getBody().get("videoAssistidoEm").isNull()).isFalse();
        assertThat(assistido.getBody().get("concluidoEm").isNull()).isTrue();
    }

    @Test
    void inicioDeOutroIntegranteNaoAutorizaAssistencia() {
        chamar("video-iniciado");
        decorrerDuracao();
        String email = UUID.randomUUID() + "@outra.teste";
        criarIntegrante(criarEmpresa("Outra Empresa Video"), "Outro", email);
        var resposta = cliente(autenticar(email, SENHA_DE_TESTE))
                .post()
                .uri("/api/v1/atividades/{id}/video-assistido", atividadeId)
                .retrieve()
                .toEntity(JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("VIDEO_NAO_INICIADO");
    }

    @Test
    void inicioBloqueadoNaoGravaProgresso() {
        var resposta = cliente(token)
                .post()
                .uri("/api/v1/atividades/{id}/video-iniciado", bloqueadaId)
                .retrieve()
                .toEntity(JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ATIVIDADE_BLOQUEADA");
        assertThat(jdbc.queryForObject(
                        "select count(*) from progresso_atividade where usuario_id = ?", Integer.class, integranteId))
                .isZero();
    }

    @Test
    void inicioExigeSessao() {
        var resposta = clienteSemSessao()
                .post()
                .uri("/api/v1/atividades/{id}/video-iniciado", atividadeId)
                .retrieve()
                .toEntity(JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void inicioSemVideoEhRecusado() {
        jdbc.update("update atividade set video_url = null, duracao_segundos = null where id = ?", atividadeId);
        var resposta = chamar("video-iniciado");
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("VIDEO_NAO_CONFIGURADO");
        assertThat(jdbc.queryForObject(
                        "select count(*) from progresso_atividade where usuario_id = ?", Integer.class, integranteId))
                .isZero();
    }

    @Test
    void inicioDeAtividadeInexistenteRetornaNaoEncontrado() {
        var resposta = cliente(token)
                .post()
                .uri("/api/v1/atividades/{id}/video-iniciado", UUID.randomUUID())
                .retrieve()
                .toEntity(JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void leituraRetornaVideoEDuracao() {
        var resposta = cliente(token)
                .get()
                .uri("/api/v1/atividades/{id}", atividadeId)
                .retrieve()
                .toEntity(JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().get("videoUrl").asText()).isEqualTo("https://youtu.be/abcdefghijk");
        assertThat(resposta.getBody().get("duracaoSegundos").asInt()).isEqualTo(120);
    }

    @Test
    void bancoRecusaDominioExternoEDuracaoAusente() {
        assertThatThrownBy(() -> jdbc.update(
                        "update atividade set video_url = 'https://youtube.com.exemplo.org/watch?v=abcdefghijk' where id = ?",
                        atividadeId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update atividade set duracao_segundos = null where id = ?", atividadeId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
