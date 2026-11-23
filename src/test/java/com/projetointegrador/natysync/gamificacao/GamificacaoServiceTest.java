package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.gamificacao.dto.ConquistaResponse;
import com.projetointegrador.natysync.gamificacao.dto.GamificacaoResponse;
import com.projetointegrador.natysync.gamificacao.dto.PosicaoRankingResponse;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GamificacaoServiceTest {

    @Test
    void buscaPerfilComRankingSequenciaEConquistas() {
        UUID usuarioId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        LocalDate hoje = LocalDate.now(Empresa.FUSO_PADRAO);
        OffsetDateTime obtidaEm = OffsetDateTime.now();

        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("Ana Souza");
        usuario.setEmpresa(empresa);
        usuario.setAtivo(true);

        Conquista conquista = mock(Conquista.class);
        UUID conquistaId = UUID.randomUUID();
        when(conquista.getId()).thenReturn(conquistaId);
        when(conquista.getCodigo()).thenReturn("SEQUENCIA_3_DIAS");
        when(conquista.getNome()).thenReturn("Três dias seguidos");
        when(conquista.getDescricao()).thenReturn("Estudou três dias seguidos");

        ConquistaObtida conquistaObtida = mock(ConquistaObtida.class);
        when(conquistaObtida.getConquistaId()).thenReturn(conquistaId);
        when(conquistaObtida.getObtidaEm()).thenReturn(obtidaEm);

        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        RankingRepository ranking = mock(RankingRepository.class);
        DiaDeEstudoRepository diasDeEstudo = mock(DiaDeEstudoRepository.class);
        ConquistaRepository conquistas = mock(ConquistaRepository.class);
        ConquistaObtidaRepository conquistasObtidas = mock(ConquistaObtidaRepository.class);
        when(usuarios.buscarNaEmpresa(usuarioId, empresaId)).thenReturn(Optional.of(usuario));
        when(ranking.posicaoDe(empresaId, usuarioId))
                .thenReturn(Optional.of(new PosicaoRankingResponse(2, usuarioId, "Ana Souza", 175)));
        when(diasDeEstudo.diasDoIntegrante(usuarioId))
                .thenReturn(List.of(hoje.minusDays(2), hoje.minusDays(1), hoje));
        when(conquistas.findAllByOrderByOrdemAsc()).thenReturn(List.of(conquista));
        when(conquistasObtidas.findByUsuarioId(usuarioId)).thenReturn(List.of(conquistaObtida));

        GamificacaoService service = new GamificacaoService(usuarios, ranking, diasDeEstudo, conquistas, conquistasObtidas);

        GamificacaoResponse resposta = service.buscar(usuarioId, empresaId);

        assertThat(resposta.integranteId()).isEqualTo(usuarioId);
        assertThat(resposta.nome()).isEqualTo("Ana Souza");
        assertThat(resposta.pontos()).isEqualTo(175);
        assertThat(resposta.posicaoNoRanking()).isEqualTo(2);
        assertThat(resposta.sequencia().atual()).isEqualTo(3);
        assertThat(resposta.sequencia().maior()).isEqualTo(3);
        assertThat(resposta.sequencia().diaCorrenteEstudado()).isTrue();
        assertThat(resposta.conquistas())
                .containsExactly(new ConquistaResponse(
                        "SEQUENCIA_3_DIAS", "Três dias seguidos", "Estudou três dias seguidos", obtidaEm));
    }
}
