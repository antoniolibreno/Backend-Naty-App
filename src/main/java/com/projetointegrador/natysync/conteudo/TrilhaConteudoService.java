package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.TrilhaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.TrilhaRequest;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.Trilha;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TrilhaConteudoService {

    private final TrilhaRepository trilhaRepository;
    private final ConteudoMapper mapper;
    private final AuditoriaDeConteudo auditoria;
    private final ProtecaoDeHistorico protecao;

    public TrilhaConteudoService(
            TrilhaRepository trilhaRepository,
            ConteudoMapper mapper,
            AuditoriaDeConteudo auditoria,
            ProtecaoDeHistorico protecao) {
        this.trilhaRepository = trilhaRepository;
        this.mapper = mapper;
        this.auditoria = auditoria;
        this.protecao = protecao;
    }

    public TrilhaAdminResponse criar(TrilhaRequest requisicao, UUID usuarioId) {
        Trilha trilha = new Trilha();
        trilha.setTitulo(requisicao.titulo());
        trilha.setDescricao(requisicao.descricao());
        trilha.setOrdem(trilhaRepository.maiorOrdem() + 1);
        trilha.setAtiva(true);
        trilhaRepository.saveAndFlush(trilha);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.TRILHA, trilha.getId(), AcaoDeConteudo.CRIACAO);
        return mapper.paraTrilha(trilha);
    }

    public TrilhaAdminResponse alterar(UUID trilhaId, TrilhaRequest requisicao, UUID usuarioId) {
        Trilha trilha = buscar(trilhaId);
        trilha.setTitulo(requisicao.titulo());
        trilha.setDescricao(requisicao.descricao());
        trilhaRepository.saveAndFlush(trilha);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.TRILHA, trilhaId, AcaoDeConteudo.ALTERACAO);
        return mapper.paraTrilha(trilha);
    }

    public TrilhaAdminResponse definirAtivacao(UUID trilhaId, boolean ativa, UUID usuarioId) {
        Trilha trilha = buscar(trilhaId);
        trilha.setAtiva(ativa);
        trilhaRepository.saveAndFlush(trilha);
        auditoria.registrar(
                usuarioId,
                EntidadeDeConteudo.TRILHA,
                trilhaId,
                ativa ? AcaoDeConteudo.ATIVACAO : AcaoDeConteudo.DESATIVACAO);
        return mapper.paraTrilha(trilha);
    }

    public void remover(UUID trilhaId, UUID usuarioId) {
        Trilha trilha = buscar(trilhaId);
        List<UUID> atividades = trilha.getModulos().stream()
                .flatMap(modulo -> modulo.getAtividades().stream())
                .map(Atividade::getId)
                .toList();
        protecao.exigirSemHistorico(atividades);
        trilhaRepository.delete(trilha);
        trilhaRepository.flush();
        auditoria.registrar(usuarioId, EntidadeDeConteudo.TRILHA, trilhaId, AcaoDeConteudo.REMOCAO);
    }

    private Trilha buscar(UUID trilhaId) {
        return trilhaRepository
                .findById(trilhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trilha nao encontrada: " + trilhaId));
    }
}
