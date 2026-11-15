package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.ConquistaResponse;
import com.projetointegrador.natysync.gamificacao.dto.EstatisticasQuizResponse;
import com.projetointegrador.natysync.gamificacao.dto.GamificacaoResponse;
import com.projetointegrador.natysync.gamificacao.dto.RankingItemResponse;
import com.projetointegrador.natysync.progresso.ProgressoAtividadeRepository;
import com.projetointegrador.natysync.progresso.RespostaTentativa;
import com.projetointegrador.natysync.progresso.TentativaQuiz;
import com.projetointegrador.natysync.progresso.TentativaQuizRepository;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.Trilha;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GamificacaoService {

    private static final String PRIMEIRA_ATIVIDADE = "PRIMEIRA_ATIVIDADE";
    private static final String PRIMEIRA_TRILHA = "PRIMEIRA_TRILHA_CONCLUIDA";
    private static final String QUIZ_PERFEITO = "QUIZ_PERFEITO";
    private static final String TRES_QUIZZES = "TRES_QUIZZES_APROVADOS";
    private static final String SEQUENCIA_3 = "SEQUENCIA_3_DIAS";
    private static final String SEQUENCIA_7 = "SEQUENCIA_7_DIAS";

    private final PontuacaoIntegranteRepository pontuacaoRepository;
    private final XpAtividadeRepository xpAtividadeRepository;
    private final SequenciaIntegranteRepository sequenciaRepository;
    private final ConquistaRepository conquistaRepository;
    private final ConquistaUsuarioRepository conquistaUsuarioRepository;
    private final ProgressoAtividadeRepository progressoRepository;
    private final TentativaQuizRepository tentativaRepository;
    private final TrilhaRepository trilhaRepository;
    private final UsuarioRepository usuarioRepository;

    public GamificacaoService(
            PontuacaoIntegranteRepository pontuacaoRepository,
            XpAtividadeRepository xpAtividadeRepository,
            SequenciaIntegranteRepository sequenciaRepository,
            ConquistaRepository conquistaRepository,
            ConquistaUsuarioRepository conquistaUsuarioRepository,
            ProgressoAtividadeRepository progressoRepository,
            TentativaQuizRepository tentativaRepository,
            TrilhaRepository trilhaRepository,
            UsuarioRepository usuarioRepository) {
        this.pontuacaoRepository = pontuacaoRepository;
        this.xpAtividadeRepository = xpAtividadeRepository;
        this.sequenciaRepository = sequenciaRepository;
        this.conquistaRepository = conquistaRepository;
        this.conquistaUsuarioRepository = conquistaUsuarioRepository;
        this.progressoRepository = progressoRepository;
        this.tentativaRepository = tentativaRepository;
        this.trilhaRepository = trilhaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void processarConclusao(Usuario usuario, Atividade atividade) {
        if (!xpAtividadeRepository.existsByUsuarioIdAndAtividadeId(usuario.getId(), atividade.getId())) {
            XpAtividade lancamento = new XpAtividade();
            lancamento.setUsuarioId(usuario.getId());
            lancamento.setAtividadeId(atividade.getId());
            lancamento.setEmpresaId(usuario.getEmpresa().getId());
            lancamento.setXp(atividade.getXp());
            xpAtividadeRepository.save(lancamento);

            PontuacaoIntegrante pontuacao = pontuacaoRepository
                    .findById(usuario.getId())
                    .orElseGet(() -> {
                        PontuacaoIntegrante nova = new PontuacaoIntegrante();
                        nova.setUsuarioId(usuario.getId());
                        nova.setEmpresaId(usuario.getEmpresa().getId());
                        return nova;
                    });
            pontuacao.setXpTotal(pontuacao.getXpTotal() + atividade.getXp());
            pontuacao.setPontuacaoAtingidaEm(agora());
            pontuacaoRepository.save(pontuacao);
        }
        atualizarSequencia(usuario);
        avaliarConquistas(usuario);
    }

    public void processarTentativa(Usuario usuario, TentativaQuiz tentativa) {
        if (tentativa.isAprovado() && tentativa.getNota() == 100) {
            conceder(usuario, QUIZ_PERFEITO);
        }
        avaliarConquistas(usuario);
    }

    @Transactional(readOnly = true)
    public GamificacaoResponse buscar(UUID integranteId, IntegranteDaRequisicao quem) {
        Usuario usuario = usuarioRepository
                .buscarNaEmpresa(integranteId, quem.empresaId())
                .filter(item -> item.getPapel() == Papel.INTEGRANTE)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Integrante nao encontrado: " + integranteId));
        PontuacaoIntegrante pontuacao =
                pontuacaoRepository.findById(usuario.getId()).orElse(null);
        SequenciaIntegrante sequencia =
                sequenciaRepository.findById(usuario.getId()).orElse(null);
        int xp = pontuacao == null ? 0 : pontuacao.getXpTotal();
        NivelCalculator.Resumo nivel = NivelCalculator.calcular(xp);
        List<TentativaQuiz> tentativas = tentativaRepository.findByUsuarioId(usuario.getId());
        EstatisticasQuizResponse quizzes = estatisticas(tentativas);
        List<ConquistaUsuario> obtidas = conquistaUsuarioRepository.findByUsuarioIdOrderByConcedidaEm(usuario.getId());
        Map<UUID, Conquista> catalogo =
                conquistaRepository
                        .findAllById(obtidas.stream()
                                .map(ConquistaUsuario::getConquistaId)
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(Conquista::getId, Function.identity()));
        List<ConquistaResponse> conquistas = obtidas.stream()
                .map(item -> catalogo.get(item.getConquistaId()))
                .filter(item -> item != null)
                .map(item -> new ConquistaResponse(
                        item.getId(),
                        item.getCodigo(),
                        item.getTitulo(),
                        item.getDescricao(),
                        obtidas.stream()
                                .filter(obtida -> obtida.getConquistaId().equals(item.getId()))
                                .findFirst()
                                .orElseThrow()
                                .getConcedidaEm()))
                .toList();
        return new GamificacaoResponse(
                integranteId,
                xp,
                nivel.nivel(),
                nivel.xpNoNivel(),
                nivel.xpParaProximoNivel(),
                nivel.percentual(),
                sequencia == null ? 0 : sequencia.getDiasAtuais(),
                sequencia == null ? 0 : sequencia.getMaiorSequencia(),
                sequencia == null ? null : sequencia.getUltimoDiaLocal(),
                quizzes,
                conquistas);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<RankingItemResponse> ranking(UUID empresaId, Pageable pageable) {
        Page<PontuacaoIntegranteRepository.RankingLinha> pagina =
                pontuacaoRepository.rankingDaEmpresa(empresaId, pageable);
        return PaginaResponse.de(pagina, item -> {
            NivelCalculator.Resumo nivel = NivelCalculator.calcular(item.getXpTotal());
            int posicao =
                    pagina.getNumber() * pagina.getSize() + pagina.getContent().indexOf(item) + 1;
            return new RankingItemResponse(
                    item.getIntegranteId(),
                    item.getNome(),
                    posicao,
                    item.getXpTotal(),
                    nivel.nivel(),
                    item.getSequenciaAtual());
        });
    }

    private EstatisticasQuizResponse estatisticas(List<TentativaQuiz> tentativas) {
        if (tentativas.isEmpty()) {
            return new EstatisticasQuizResponse(0, 0, null, 0, 0);
        }
        long respostas = 0;
        long corretas = 0;
        for (TentativaQuiz tentativa : tentativas) {
            for (RespostaTentativa resposta : tentativa.getRespostas()) {
                respostas++;
                if (resposta.isCorreta()) {
                    corretas++;
                }
            }
        }
        double media =
                tentativas.stream().mapToInt(TentativaQuiz::getNota).average().orElse(0);
        Integer melhor =
                tentativas.stream().mapToInt(TentativaQuiz::getNota).max().orElse(0);
        long aprovados = tentativas.stream().filter(TentativaQuiz::isAprovado).count();
        return new EstatisticasQuizResponse(
                tentativas.size(), aprovados, melhor, media, respostas == 0 ? 0 : corretas * 100.0 / respostas);
    }

    private void atualizarSequencia(Usuario usuario) {
        OffsetDateTime agora = agora();
        LocalDate hoje =
                agora.atZoneSameInstant(usuario.getEmpresa().getFusoHorario()).toLocalDate();
        SequenciaIntegrante sequencia = sequenciaRepository
                .findById(usuario.getId())
                .orElseGet(() -> {
                    SequenciaIntegrante nova = new SequenciaIntegrante();
                    nova.setUsuarioId(usuario.getId());
                    nova.setEmpresaId(usuario.getEmpresa().getId());
                    return nova;
                });
        if (hoje.equals(sequencia.getUltimoDiaLocal())) {
            sequencia.setUltimoEventoEm(agora);
        } else if (sequencia.getUltimoDiaLocal() != null
                && hoje.equals(sequencia.getUltimoDiaLocal().plusDays(1))) {
            sequencia.setDiasAtuais(sequencia.getDiasAtuais() + 1);
            sequencia.setMaiorSequencia(Math.max(sequencia.getMaiorSequencia(), sequencia.getDiasAtuais()));
            sequencia.setUltimoDiaLocal(hoje);
            sequencia.setUltimoEventoEm(agora);
        } else {
            sequencia.setDiasAtuais(1);
            sequencia.setMaiorSequencia(Math.max(sequencia.getMaiorSequencia(), 1));
            sequencia.setUltimoDiaLocal(hoje);
            sequencia.setUltimoEventoEm(agora);
        }
        sequenciaRepository.save(sequencia);
    }

    private void avaliarConquistas(Usuario usuario) {
        if (progressoRepository.countByUsuarioIdAndConcluidoEmIsNotNull(usuario.getId()) >= 1) {
            conceder(usuario, PRIMEIRA_ATIVIDADE);
        }
        if (concluiuTrilha(usuario.getId())) {
            conceder(usuario, PRIMEIRA_TRILHA);
        }
        if (tentativaRepository.countDistinctQuizIdByUsuarioIdAndAprovadoTrue(usuario.getId()) >= 3) {
            conceder(usuario, TRES_QUIZZES);
        }
        SequenciaIntegrante sequencia =
                sequenciaRepository.findById(usuario.getId()).orElse(null);
        if (sequencia != null) {
            if (sequencia.getMaiorSequencia() >= 3) {
                conceder(usuario, SEQUENCIA_3);
            }
            if (sequencia.getMaiorSequencia() >= 7) {
                conceder(usuario, SEQUENCIA_7);
            }
        }
    }

    private boolean concluiuTrilha(UUID usuarioId) {
        Set<UUID> concluidas = progressoRepository.findByUsuarioId(usuarioId).stream()
                .filter(item -> item.getConcluidoEm() != null)
                .map(item -> item.getAtividade().getId())
                .collect(Collectors.toSet());
        return trilhaRepository.findAll().stream().anyMatch(trilha -> todasAtividadesConcluidas(trilha, concluidas));
    }

    private boolean todasAtividadesConcluidas(Trilha trilha, Set<UUID> concluidas) {
        return !trilha.getModulos().isEmpty()
                && trilha.getModulos().stream()
                        .flatMap(modulo -> modulo.getAtividades().stream())
                        .allMatch(atividade -> concluidas.contains(atividade.getId()));
    }

    private void conceder(Usuario usuario, String codigo) {
        Conquista conquista =
                conquistaRepository.findByCodigoAndAtivaTrue(codigo).orElse(null);
        if (conquista != null
                && !conquistaUsuarioRepository.existsByUsuarioIdAndConquistaId(usuario.getId(), conquista.getId())) {
            ConquistaUsuario obtida = new ConquistaUsuario();
            obtida.setUsuarioId(usuario.getId());
            obtida.setConquistaId(conquista.getId());
            conquistaUsuarioRepository.save(obtida);
        }
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
