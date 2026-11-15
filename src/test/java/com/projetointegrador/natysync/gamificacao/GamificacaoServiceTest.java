package com.projetointegrador.natysync.gamificacao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.progresso.ProgressoAtividadeRepository;
import com.projetointegrador.natysync.progresso.TentativaQuizRepository;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GamificacaoServiceTest {

    @Test
    void naoConcedeXpDuasVezesParaAMesmaAtividade() {
        UUID usuarioId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID atividadeId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        usuario.setEmpresa(empresa);
        Atividade atividade = new Atividade();
        atividade.setId(atividadeId);
        atividade.setXp(25);

        PontuacaoIntegranteRepository pontuacao = mock(PontuacaoIntegranteRepository.class);
        XpAtividadeRepository xp = mock(XpAtividadeRepository.class);
        SequenciaIntegranteRepository sequencia = mock(SequenciaIntegranteRepository.class);
        ConquistaRepository conquistas = mock(ConquistaRepository.class);
        ConquistaUsuarioRepository obtidas = mock(ConquistaUsuarioRepository.class);
        ProgressoAtividadeRepository progresso = mock(ProgressoAtividadeRepository.class);
        TentativaQuizRepository tentativas = mock(TentativaQuizRepository.class);
        TrilhaRepository trilhas = mock(TrilhaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(xp.existsByUsuarioIdAndAtividadeId(usuarioId, atividadeId)).thenReturn(false, true);
        when(pontuacao.findById(usuarioId)).thenReturn(Optional.empty());
        when(sequencia.findById(usuarioId)).thenReturn(Optional.empty());
        when(progresso.findByUsuarioId(usuarioId)).thenReturn(java.util.List.of());
        when(tentativas.countDistinctQuizIdByUsuarioIdAndAprovadoTrue(usuarioId))
                .thenReturn(0L);
        when(trilhas.findAll()).thenReturn(java.util.List.of());
        when(conquistas.findByCodigoAndAtivaTrue(any())).thenReturn(Optional.empty());
        GamificacaoService service = new GamificacaoService(
                pontuacao, xp, sequencia, conquistas, obtidas, progresso, tentativas, trilhas, usuarios);

        service.processarConclusao(usuario, atividade);
        service.processarConclusao(usuario, atividade);

        verify(xp, times(1)).save(any(XpAtividade.class));
        verify(pontuacao, times(1)).save(any(PontuacaoIntegrante.class));
    }
}
