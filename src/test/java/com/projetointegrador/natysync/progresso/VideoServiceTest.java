package com.projetointegrador.natysync.progresso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import com.projetointegrador.natysync.trilha.Modulo;
import com.projetointegrador.natysync.trilha.Quiz;
import com.projetointegrador.natysync.trilha.QuizRepository;
import com.projetointegrador.natysync.trilha.Trilha;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class VideoServiceTest {

    private final IntegranteDaRequisicao integrante =
            new IntegranteDaRequisicao(UUID.randomUUID(), UUID.randomUUID(), Papel.INTEGRANTE);
    private final ProgressoAtividadeRepository progressos = mock(ProgressoAtividadeRepository.class);
    private final AtividadeRepository atividades = mock(AtividadeRepository.class);
    private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
    private final ProgressoService service = new ProgressoService(
            mock(TrilhaRepository.class),
            atividades,
            mock(UsuarioRepository.class),
            progressos,
            new TrilhaProgressoMontador(),
            mock(QuizRepository.class),
            mock(EntityManager.class),
            eventos);
    private Atividade atividade;
    private ProgressoAtividade progresso;

    @BeforeEach
    void preparar() {
        Trilha trilha = new Trilha();
        Modulo modulo = new Modulo();
        modulo.setOrdem(1);
        modulo.setTrilha(trilha);
        trilha.getModulos().add(modulo);
        atividade = new Atividade();
        atividade.setId(UUID.randomUUID());
        atividade.setOrdem(1);
        atividade.setModulo(modulo);
        atividade.setVideoUrl("https://youtu.be/abcdefghijk");
        atividade.setDuracaoSegundos(120);
        modulo.getAtividades().add(atividade);
        progresso = new ProgressoAtividade();
        progresso.setAtividade(atividade);
        when(atividades.findById(atividade.getId())).thenReturn(Optional.of(atividade));
        when(progressos.findByUsuarioId(integrante.usuarioId())).thenReturn(List.of(progresso));
        when(progressos.save(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @Test
    void semInicioRecusaSemGravarAssistencia() {
        assertThatThrownBy(() -> service.registrarVideoAssistido(atividade.getId(), integrante))
                .isInstanceOfSatisfying(ConflitoException.class, erro -> assertThat(erro.getCodigo())
                        .isEqualTo("VIDEO_NAO_INICIADO"));
        verify(progressos, never()).save(any());
        verify(eventos, never()).publishEvent(any(Object.class));
        assertThat(progresso.getVideoAssistidoEm()).isNull();
    }

    @Test
    void inicioRecenteRecusaSemGravarConclusao() {
        progresso.setVideoIniciadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        assertThatThrownBy(() -> service.registrarVideoAssistido(atividade.getId(), integrante))
                .isInstanceOfSatisfying(ConflitoException.class, erro -> assertThat(erro.getCodigo())
                        .isEqualTo("VIDEO_TEMPO_INSUFICIENTE"));
        verify(progressos, never()).save(any());
        assertThat(progresso.getConcluidoEm()).isNull();
    }

    @Test
    void duracaoCumpridaConcluiAtividadeSemQuizERepeticaoPreservaInstantes() {
        progresso.setVideoIniciadoEm(OffsetDateTime.now(ZoneOffset.UTC)
                .truncatedTo(java.time.temporal.ChronoUnit.MICROS)
                .minusSeconds(120));
        var primeira = service.registrarVideoAssistido(atividade.getId(), integrante);
        var repetida = service.registrarVideoAssistido(atividade.getId(), integrante);
        assertThat(primeira.estado()).isEqualTo(EstadoAtividade.CONCLUIDO);
        assertThat(primeira.videoAssistidoEm()).isNotNull();
        assertThat(primeira.concluidoEm()).isNotNull();
        assertThat(repetida).isEqualTo(primeira);
        EstudoRegistrado estudo = estudoPublicadoUmaVez();
        assertThat(estudo.atividadeConcluida()).isTrue();
        assertThat(estudo.ocorridoEm()).isEqualTo(primeira.concluidoEm());
        assertThat(estudo.usuarioId()).isEqualTo(integrante.usuarioId());
        assertThat(estudo.empresaId()).isEqualTo(integrante.empresaId());
        assertThat(estudo.atividadeId()).isEqualTo(atividade.getId());
    }

    @Test
    void videoComQuizNaoConclui() {
        atividade.setQuiz(new Quiz());
        progresso.setVideoIniciadoEm(OffsetDateTime.now(ZoneOffset.UTC)
                .truncatedTo(java.time.temporal.ChronoUnit.MICROS)
                .minusSeconds(120));
        var resposta = service.registrarVideoAssistido(atividade.getId(), integrante);
        assertThat(resposta.videoAssistidoEm()).isNotNull();
        assertThat(resposta.concluidoEm()).isNull();
        assertThat(resposta.estado()).isEqualTo(EstadoAtividade.DISPONIVEL);
        assertThat(estudoPublicadoUmaVez().atividadeConcluida()).isFalse();
    }

    @Test
    void inicioRepetidoNaoReiniciaContagem() {
        var primeira = service.registrarVideoIniciado(atividade.getId(), integrante);
        var repetida = service.registrarVideoIniciado(atividade.getId(), integrante);
        assertThat(primeira.videoIniciadoEm()).isNotNull();
        assertThat(repetida).isEqualTo(primeira);
        verify(eventos, never()).publishEvent(any(Object.class));
    }

    private EstudoRegistrado estudoPublicadoUmaVez() {
        ArgumentCaptor<Object> publicados = ArgumentCaptor.forClass(Object.class);
        verify(eventos, times(1)).publishEvent(publicados.capture());
        return (EstudoRegistrado) publicados.getValue();
    }
}
