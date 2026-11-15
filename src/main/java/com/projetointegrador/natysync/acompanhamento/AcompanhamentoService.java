package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.acompanhamento.dto.FunilDeAtividadeResponse;
import com.projetointegrador.natysync.acompanhamento.dto.IntegranteAcompanhadoResponse;
import com.projetointegrador.natysync.acompanhamento.dto.MetricasDaEmpresaResponse;
import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.Papel;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AcompanhamentoService {

    private final EmpresaRepository empresaRepository;
    private final CalculadoraDeAcompanhamento calculadora;
    private final IntegrantesAcompanhadosRepository integrantesRepository;
    private final ConclusoesRepository conclusoesRepository;

    public AcompanhamentoService(
            EmpresaRepository empresaRepository,
            CalculadoraDeAcompanhamento calculadora,
            IntegrantesAcompanhadosRepository integrantesRepository,
            ConclusoesRepository conclusoesRepository) {
        this.empresaRepository = empresaRepository;
        this.calculadora = calculadora;
        this.integrantesRepository = integrantesRepository;
        this.conclusoesRepository = conclusoesRepository;
    }

    public PaginaResponse<IntegranteAcompanhadoResponse> listar(
            UUID empresaId, SituacaoDeAcompanhamento situacao, Pageable pagina) {
        List<IntegranteAcompanhadoResponse> todos = todos(empresaId, situacao);
        PageRequest pedido = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize());
        int inicio = (int) Math.min(pedido.getOffset(), todos.size());
        int fim = Math.min(inicio + pedido.getPageSize(), todos.size());
        return PaginaResponse.de(new PageImpl<>(todos.subList(inicio, fim), pedido, todos.size()), Function.identity());
    }

    public List<IntegranteAcompanhadoResponse> todos(UUID empresaId, SituacaoDeAcompanhamento situacao) {
        List<IntegranteAcompanhadoResponse> integrantes = calculadora.calcular(buscarEmpresa(empresaId), agora());
        if (situacao == null) {
            return integrantes;
        }
        return integrantes.stream()
                .filter(integrante -> integrante.situacao() == situacao)
                .toList();
    }

    public MetricasDaEmpresaResponse metricas(UUID empresaId) {
        List<IntegranteAcompanhadoResponse> integrantes = todos(empresaId, null);
        int total = integrantes.size();
        int iniciaram = (int) integrantes.stream()
                .filter(integrante -> integrante.atividadesConcluidas() > 0)
                .count();
        int somaDosPercentuais = integrantes.stream()
                .mapToInt(IntegranteAcompanhadoResponse::percentualConcluido)
                .sum();
        Map<SituacaoDeAcompanhamento, Long> porSituacao = integrantes.stream()
                .collect(Collectors.groupingBy(IntegranteAcompanhadoResponse::situacao, Collectors.counting()));
        return new MetricasDaEmpresaResponse(
                empresaId,
                total,
                total == 0 ? 0 : iniciaram * 100 / total,
                total == 0 ? 0 : somaDosPercentuais / total,
                porSituacao.getOrDefault(SituacaoDeAcompanhamento.CONCLUIU, 0L).intValue(),
                porSituacao.getOrDefault(SituacaoDeAcompanhamento.AVANCANDO, 0L).intValue(),
                porSituacao.getOrDefault(SituacaoDeAcompanhamento.PARADO, 0L).intValue(),
                porSituacao
                        .getOrDefault(SituacaoDeAcompanhamento.SEM_INICIO, 0L)
                        .intValue());
    }

    public List<FunilDeAtividadeResponse> funil(UUID empresaId) {
        if (empresaId != null) {
            buscarEmpresa(empresaId);
        }
        long considerados = empresaId == null
                ? integrantesRepository.contarConsideradosDeTodasAsEmpresas(Papel.NATY)
                : integrantesRepository.contarConsideradosDaEmpresa(empresaId, Papel.NATY);
        Map<UUID, Long> conclusoes = (empresaId == null
                        ? conclusoesRepository.contarConclusoesDeTodasAsEmpresas(Papel.NATY)
                        : conclusoesRepository.contarConclusoesDaEmpresa(empresaId, Papel.NATY))
                .stream().collect(Collectors.toMap(ContagemPorAtividade::atividadeId, ContagemPorAtividade::total));

        List<FunilDeAtividadeResponse> funil = new ArrayList<>();
        UUID trilhaAnterior = null;
        UUID atividadeAnterior = null;
        for (CalculadoraDeAcompanhamento.AtividadeAtiva atividade : calculadora.atividadesAtivasEmOrdem()) {
            boolean primeiraDaTrilha = !atividade.trilhaId().equals(trilhaAnterior);
            long chegaram = primeiraDaTrilha ? considerados : conclusoes.getOrDefault(atividadeAnterior, 0L);
            long passaram = conclusoes.getOrDefault(atividade.atividadeId(), 0L);
            funil.add(new FunilDeAtividadeResponse(
                    atividade.atividadeId(),
                    atividade.trilhaId(),
                    atividade.titulo(),
                    chegaram,
                    passaram,
                    chegaram == 0 ? 0 : (int) (passaram * 100 / chegaram)));
            trilhaAnterior = atividade.trilhaId();
            atividadeAnterior = atividade.atividadeId();
        }
        return funil;
    }

    private Empresa buscarEmpresa(UUID empresaId) {
        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada: " + empresaId));
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
