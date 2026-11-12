package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AtividadeAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.AtividadeCriacaoRequest;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import com.projetointegrador.natysync.trilha.Modulo;
import com.projetointegrador.natysync.trilha.ModuloRepository;
import com.projetointegrador.natysync.trilha.TrilhaMapper;
import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AtividadeConteudoService {

    private final ModuloRepository moduloRepository;
    private final AtividadeRepository atividadeRepository;
    private final TrilhaMapper trilhaMapper;
    private final AuditoriaDeConteudo auditoria;
    private final ProtecaoDeHistorico protecao;

    public AtividadeConteudoService(
            ModuloRepository moduloRepository,
            AtividadeRepository atividadeRepository,
            TrilhaMapper trilhaMapper,
            AuditoriaDeConteudo auditoria,
            ProtecaoDeHistorico protecao) {
        this.moduloRepository = moduloRepository;
        this.atividadeRepository = atividadeRepository;
        this.trilhaMapper = trilhaMapper;
        this.auditoria = auditoria;
        this.protecao = protecao;
    }

    public AtividadeDetalheResponse criar(AtividadeCriacaoRequest requisicao, UUID usuarioId) {
        Modulo modulo = moduloRepository
                .travarPorId(requisicao.moduloId())
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Modulo nao encontrado: " + requisicao.moduloId()));
        Atividade atividade = new Atividade();
        atividade.setModulo(modulo);
        atividade.setTitulo(requisicao.titulo());
        atividade.setDescricao(requisicao.descricao());
        atividade.setOrdem(atividadeRepository.maiorOrdemDoModulo(modulo.getId()) + 1);
        atividade.setImagemUrl(requisicao.imagemUrl());
        atividade.setVideoUrl(requisicao.videoUrl());
        atividade.setDuracaoSegundos(requisicao.duracaoSegundos());
        atividade.setXp(requisicao.xp());
        atividadeRepository.saveAndFlush(atividade);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.ATIVIDADE, atividade.getId(), AcaoDeConteudo.CRIACAO);
        return trilhaMapper.paraAtividadeDetalhe(atividade);
    }

    public AtividadeDetalheResponse alterar(UUID atividadeId, AtividadeAlteracaoRequest requisicao, UUID usuarioId) {
        Atividade atividade = buscar(atividadeId);
        atividade.setTitulo(requisicao.titulo());
        atividade.setDescricao(requisicao.descricao());
        atividade.setImagemUrl(requisicao.imagemUrl());
        atividade.setVideoUrl(requisicao.videoUrl());
        atividade.setDuracaoSegundos(requisicao.duracaoSegundos());
        atividade.setXp(requisicao.xp());
        atividadeRepository.saveAndFlush(atividade);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.ATIVIDADE, atividadeId, AcaoDeConteudo.ALTERACAO);
        return trilhaMapper.paraAtividadeDetalhe(atividade);
    }

    public void remover(UUID atividadeId, UUID usuarioId) {
        Atividade atividade = buscar(atividadeId);
        protecao.exigirSemHistorico(List.of(atividadeId));
        atividadeRepository.delete(atividade);
        atividadeRepository.flush();
        auditoria.registrar(usuarioId, EntidadeDeConteudo.ATIVIDADE, atividadeId, AcaoDeConteudo.REMOCAO);
    }

    private Atividade buscar(UUID atividadeId) {
        return atividadeRepository
                .findById(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade nao encontrada: " + atividadeId));
    }
}
