package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.ModuloAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.ModuloCriacaoRequest;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.Modulo;
import com.projetointegrador.natysync.trilha.ModuloRepository;
import com.projetointegrador.natysync.trilha.Trilha;
import com.projetointegrador.natysync.trilha.TrilhaMapper;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.trilha.dto.ModuloResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModuloConteudoService {

    private final TrilhaRepository trilhaRepository;
    private final ModuloRepository moduloRepository;
    private final TrilhaMapper trilhaMapper;
    private final AuditoriaDeConteudo auditoria;
    private final ProtecaoDeHistorico protecao;

    public ModuloConteudoService(
            TrilhaRepository trilhaRepository,
            ModuloRepository moduloRepository,
            TrilhaMapper trilhaMapper,
            AuditoriaDeConteudo auditoria,
            ProtecaoDeHistorico protecao) {
        this.trilhaRepository = trilhaRepository;
        this.moduloRepository = moduloRepository;
        this.trilhaMapper = trilhaMapper;
        this.auditoria = auditoria;
        this.protecao = protecao;
    }

    public ModuloResponse criar(ModuloCriacaoRequest requisicao, UUID usuarioId) {
        Trilha trilha = trilhaRepository
                .travarPorId(requisicao.trilhaId())
                .orElseThrow(
                        () -> new RecursoNaoEncontradoException("Trilha nao encontrada: " + requisicao.trilhaId()));
        Modulo modulo = new Modulo();
        modulo.setTrilha(trilha);
        modulo.setTitulo(requisicao.titulo());
        modulo.setDescricao(requisicao.descricao());
        modulo.setOrdem(moduloRepository.maiorOrdemDaTrilha(trilha.getId()) + 1);
        moduloRepository.saveAndFlush(modulo);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.MODULO, modulo.getId(), AcaoDeConteudo.CRIACAO);
        return trilhaMapper.paraModulo(modulo);
    }

    public ModuloResponse alterar(UUID moduloId, ModuloAlteracaoRequest requisicao, UUID usuarioId) {
        Modulo modulo = buscar(moduloId);
        modulo.setTitulo(requisicao.titulo());
        modulo.setDescricao(requisicao.descricao());
        moduloRepository.saveAndFlush(modulo);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.MODULO, moduloId, AcaoDeConteudo.ALTERACAO);
        return trilhaMapper.paraModulo(modulo);
    }

    public void remover(UUID moduloId, UUID usuarioId) {
        Modulo modulo = buscar(moduloId);
        List<UUID> atividades =
                modulo.getAtividades().stream().map(Atividade::getId).toList();
        protecao.exigirSemHistorico(atividades);
        moduloRepository.delete(modulo);
        moduloRepository.flush();
        auditoria.registrar(usuarioId, EntidadeDeConteudo.MODULO, moduloId, AcaoDeConteudo.REMOCAO);
    }

    private Modulo buscar(UUID moduloId) {
        return moduloRepository
                .findById(moduloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Modulo nao encontrado: " + moduloId));
    }
}
