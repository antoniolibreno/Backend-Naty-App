package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProtecaoDeHistorico {

    private final AtividadeRepository atividadeRepository;

    public ProtecaoDeHistorico(AtividadeRepository atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    public void exigirSemHistorico(Collection<UUID> atividadeIds) {
        if (!atividadeIds.isEmpty() && atividadeRepository.temHistoricoDeEstudo(atividadeIds)) {
            throw new ConflitoException(
                    "CONTEUDO_COM_PROGRESSO",
                    "Conteudo com progresso, tentativa ou ponto registrado nao e removido. Desative a trilha.");
        }
    }
}
