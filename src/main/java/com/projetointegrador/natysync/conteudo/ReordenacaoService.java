package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import com.projetointegrador.natysync.trilha.ModuloRepository;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReordenacaoService {

    private static final int DESLOCAMENTO = 1_000_000;

    private final TrilhaRepository trilhaRepository;
    private final ModuloRepository moduloRepository;
    private final AtividadeRepository atividadeRepository;
    private final AuditoriaDeConteudo auditoria;

    public ReordenacaoService(
            TrilhaRepository trilhaRepository,
            ModuloRepository moduloRepository,
            AtividadeRepository atividadeRepository,
            AuditoriaDeConteudo auditoria) {
        this.trilhaRepository = trilhaRepository;
        this.moduloRepository = moduloRepository;
        this.atividadeRepository = atividadeRepository;
        this.auditoria = auditoria;
    }

    public void reordenarModulos(UUID trilhaId, List<UUID> ids, UUID usuarioId) {
        trilhaRepository
                .travarPorId(trilhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trilha nao encontrada: " + trilhaId));
        exigirOsMesmosIrmaos(moduloRepository.idsDaTrilha(trilhaId), ids);
        moduloRepository.deslocarOrdemDaTrilha(trilhaId, DESLOCAMENTO);
        numerar(ids, moduloRepository::definirOrdem);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.TRILHA, trilhaId, AcaoDeConteudo.REORDENACAO);
    }

    public void reordenarAtividades(UUID moduloId, List<UUID> ids, UUID usuarioId) {
        moduloRepository
                .travarPorId(moduloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Modulo nao encontrado: " + moduloId));
        exigirOsMesmosIrmaos(atividadeRepository.idsDoModulo(moduloId), ids);
        atividadeRepository.deslocarOrdemDoModulo(moduloId, DESLOCAMENTO);
        numerar(ids, atividadeRepository::definirOrdem);
        auditoria.registrar(usuarioId, EntidadeDeConteudo.MODULO, moduloId, AcaoDeConteudo.REORDENACAO);
    }

    private void exigirOsMesmosIrmaos(List<UUID> atuais, List<UUID> pedidos) {
        boolean semRepeticao = new HashSet<>(pedidos).size() == pedidos.size();
        if (!semRepeticao || pedidos.size() != atuais.size() || !new HashSet<>(atuais).containsAll(pedidos)) {
            throw new ConflitoException(
                    "ORDEM_INVALIDA", "A lista deve conter exatamente os irmaos atuais, sem repeticao.");
        }
    }

    private void numerar(List<UUID> ids, BiConsumer<UUID, Integer> definirOrdem) {
        for (int posicao = 0; posicao < ids.size(); posicao++) {
            definirOrdem.accept(ids.get(posicao), posicao + 1);
        }
    }
}
