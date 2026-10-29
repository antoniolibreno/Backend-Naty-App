package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.ConquistaResponse;
import com.projetointegrador.natysync.gamificacao.dto.GamificacaoResponse;
import com.projetointegrador.natysync.gamificacao.dto.PosicaoRankingResponse;
import com.projetointegrador.natysync.gamificacao.dto.SequenciaResponse;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GamificacaoService {

    private final UsuarioRepository usuarioRepository;
    private final RankingRepository rankingRepository;
    private final DiaDeEstudoRepository diaDeEstudoRepository;
    private final ConquistaRepository conquistaRepository;
    private final ConquistaObtidaRepository conquistaObtidaRepository;

    public GamificacaoService(
            UsuarioRepository usuarioRepository,
            RankingRepository rankingRepository,
            DiaDeEstudoRepository diaDeEstudoRepository,
            ConquistaRepository conquistaRepository,
            ConquistaObtidaRepository conquistaObtidaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rankingRepository = rankingRepository;
        this.diaDeEstudoRepository = diaDeEstudoRepository;
        this.conquistaRepository = conquistaRepository;
        this.conquistaObtidaRepository = conquistaObtidaRepository;
    }

    public GamificacaoResponse buscar(UUID integranteId, UUID empresaId) {
        Usuario integrante = usuarioRepository
                .buscarNaEmpresa(integranteId, empresaId)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> naoEncontrado(integranteId));
        PosicaoRankingResponse posicao =
                rankingRepository.posicaoDe(empresaId, integranteId).orElseThrow(() -> naoEncontrado(integranteId));
        LocalDate diaCorrente = LocalDate.now(integrante.getEmpresa().getFusoHorario());
        SequenciaDeDias sequencia =
                SequenciaDeDias.calcular(diaDeEstudoRepository.diasDoIntegrante(integranteId), diaCorrente);
        return new GamificacaoResponse(
                integranteId,
                integrante.getNome(),
                posicao.pontos(),
                posicao.posicao(),
                new SequenciaResponse(sequencia.atual(), sequencia.maior(), sequencia.diaCorrenteEstudado()),
                conquistas(integranteId));
    }

    private List<ConquistaResponse> conquistas(UUID integranteId) {
        Map<UUID, OffsetDateTime> obtidas = conquistaObtidaRepository.findByUsuarioId(integranteId).stream()
                .collect(Collectors.toMap(ConquistaObtida::getConquistaId, ConquistaObtida::getObtidaEm));
        return conquistaRepository.findAllByOrderByOrdemAsc().stream()
                .map(conquista -> new ConquistaResponse(
                        conquista.getCodigo(),
                        conquista.getNome(),
                        conquista.getDescricao(),
                        obtidas.get(conquista.getId())))
                .toList();
    }

    private RecursoNaoEncontradoException naoEncontrado(UUID integranteId) {
        return new RecursoNaoEncontradoException("Integrante nao encontrado: " + integranteId);
    }
}
