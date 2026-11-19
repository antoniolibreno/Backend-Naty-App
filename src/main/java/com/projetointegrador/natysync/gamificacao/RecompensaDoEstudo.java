package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.progresso.EstudoRegistrado;
import com.projetointegrador.natysync.trilha.Atividade;
import com.projetointegrador.natysync.trilha.AtividadeRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecompensaDoEstudo {

    private final EmpresaRepository empresaRepository;
    private final AtividadeRepository atividadeRepository;
    private final DiaDeEstudoRepository diaDeEstudoRepository;
    private final PontoConcedidoRepository pontoConcedidoRepository;
    private final AvaliadorDeConquistas avaliadorDeConquistas;

    public RecompensaDoEstudo(
            EmpresaRepository empresaRepository,
            AtividadeRepository atividadeRepository,
            DiaDeEstudoRepository diaDeEstudoRepository,
            PontoConcedidoRepository pontoConcedidoRepository,
            AvaliadorDeConquistas avaliadorDeConquistas) {
        this.empresaRepository = empresaRepository;
        this.atividadeRepository = atividadeRepository;
        this.diaDeEstudoRepository = diaDeEstudoRepository;
        this.pontoConcedidoRepository = pontoConcedidoRepository;
        this.avaliadorDeConquistas = avaliadorDeConquistas;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void recompensar(EstudoRegistrado estudo) {
        ZoneId fuso = empresaRepository
                .findById(estudo.empresaId())
                .orElseThrow(() -> new IllegalStateException("Empresa do estudo inexistente: " + estudo.empresaId()))
                .getFusoHorario();
        LocalDate dia = estudo.ocorridoEm().atZoneSameInstant(fuso).toLocalDate();
        diaDeEstudoRepository.registrar(estudo.usuarioId(), dia);
        if (estudo.atividadeConcluida()) {
            Atividade atividade = atividadeRepository
                    .findById(estudo.atividadeId())
                    .orElseThrow(() ->
                            new IllegalStateException("Atividade do estudo inexistente: " + estudo.atividadeId()));
            pontoConcedidoRepository.conceder(
                    estudo.usuarioId(), atividade.getId(), atividade.getXp(), estudo.ocorridoEm());
        }
        avaliadorDeConquistas.avaliar(estudo.usuarioId(), dia, estudo.ocorridoEm());
    }
}
