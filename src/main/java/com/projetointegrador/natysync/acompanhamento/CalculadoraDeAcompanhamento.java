package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.acompanhamento.dto.AtividadeAtualResponse;
import com.projetointegrador.natysync.acompanhamento.dto.IntegranteAcompanhadoResponse;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraDeAcompanhamento {

    private static final Comparator<IntegranteAcompanhadoResponse> POR_PRIORIDADE = Comparator.comparing(
                    IntegranteAcompanhadoResponse::situacao)
            .thenComparing(Comparator.comparingLong(IntegranteAcompanhadoResponse::diasSemConcluir)
                    .reversed())
            .thenComparing(integrante -> integrante.nome().toLowerCase())
            .thenComparing(IntegranteAcompanhadoResponse::integranteId);

    private final IntegrantesAcompanhadosRepository integrantesRepository;
    private final ConclusoesRepository conclusoesRepository;
    private final TrilhaRepository trilhaRepository;
    private final int diasParado;

    public CalculadoraDeAcompanhamento(
            IntegrantesAcompanhadosRepository integrantesRepository,
            ConclusoesRepository conclusoesRepository,
            TrilhaRepository trilhaRepository,
            @Value("${app.acompanhamento.dias-parado}") int diasParado) {
        this.integrantesRepository = integrantesRepository;
        this.conclusoesRepository = conclusoesRepository;
        this.trilhaRepository = trilhaRepository;
        this.diasParado = diasParado;
    }

    public record AtividadeAtiva(UUID atividadeId, UUID trilhaId, String titulo) {}

    public List<IntegranteAcompanhadoResponse> calcular(Empresa empresa, OffsetDateTime agora) {
        List<Usuario> integrantes =
                integrantesRepository.findByEmpresaIdAndAtivoTrueAndPapelNot(empresa.getId(), Papel.NATY);
        if (integrantes.isEmpty()) {
            return List.of();
        }
        List<AtividadeAtiva> atividades = atividadesAtivasEmOrdem();
        Map<UUID, List<Conclusao>> conclusoesPorIntegrante =
                conclusoesRepository
                        .conclusoesDe(integrantes.stream().map(Usuario::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(Conclusao::usuarioId));
        return integrantes.stream()
                .map(integrante -> calcularDe(
                        integrante,
                        atividades,
                        conclusoesPorIntegrante.getOrDefault(integrante.getId(), List.of()),
                        empresa.getFusoHorario(),
                        agora))
                .sorted(POR_PRIORIDADE)
                .toList();
    }

    public IntegranteAcompanhadoResponse calcularDe(
            Usuario integrante,
            List<AtividadeAtiva> atividades,
            Collection<Conclusao> conclusoes,
            ZoneId fuso,
            OffsetDateTime agora) {
        Set<UUID> idsAtivos =
                atividades.stream().map(AtividadeAtiva::atividadeId).collect(Collectors.toSet());
        Map<UUID, OffsetDateTime> concluidasAtivas = new HashMap<>();
        for (Conclusao conclusao : conclusoes) {
            if (idsAtivos.contains(conclusao.atividadeId())) {
                concluidasAtivas.put(conclusao.atividadeId(), conclusao.concluidoEm());
            }
        }
        int total = atividades.size();
        int concluidas = concluidasAtivas.size();
        OffsetDateTime ultima = concluidasAtivas.values().stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
        AtividadeAtualResponse atual = atividades.stream()
                .filter(atividade -> !concluidasAtivas.containsKey(atividade.atividadeId()))
                .findFirst()
                .map(atividade ->
                        new AtividadeAtualResponse(atividade.atividadeId(), atividade.trilhaId(), atividade.titulo()))
                .orElse(null);
        OffsetDateTime referencia = ultima != null ? ultima : integrante.getCriadoEm();
        LocalDate diaDaReferencia = referencia.atZoneSameInstant(fuso).toLocalDate();
        LocalDate hoje = agora.atZoneSameInstant(fuso).toLocalDate();
        long dias = ChronoUnit.DAYS.between(diaDaReferencia, hoje);

        return new IntegranteAcompanhadoResponse(
                integrante.getId(),
                integrante.getNome(),
                integrante.getEmail(),
                integrante.getPapel(),
                situacaoDe(total, concluidas, dias),
                concluidas,
                total,
                total == 0 ? 0 : concluidas * 100 / total,
                atual,
                ultima,
                dias);
    }

    private SituacaoDeAcompanhamento situacaoDe(int total, int concluidas, long dias) {
        if (total > 0 && concluidas == total) {
            return SituacaoDeAcompanhamento.CONCLUIU;
        }
        if (dias >= diasParado) {
            return SituacaoDeAcompanhamento.PARADO;
        }
        return concluidas == 0 ? SituacaoDeAcompanhamento.SEM_INICIO : SituacaoDeAcompanhamento.AVANCANDO;
    }

    public List<AtividadeAtiva> atividadesAtivasEmOrdem() {
        return trilhaRepository.findAllByAtivaTrueOrderByOrdemAsc().stream()
                .flatMap(trilha -> trilha.getModulos().stream())
                .flatMap(modulo -> modulo.getAtividades().stream()
                        .map(atividade -> new AtividadeAtiva(
                                atividade.getId(), modulo.getTrilha().getId(), atividade.getTitulo())))
                .toList();
    }
}
