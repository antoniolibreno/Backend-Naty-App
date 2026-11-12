package com.projetointegrador.natysync.acompanhamento;

import com.projetointegrador.natysync.acompanhamento.dto.FunilDeAtividadeResponse;
import com.projetointegrador.natysync.acompanhamento.dto.IntegranteAcompanhadoResponse;
import com.projetointegrador.natysync.acompanhamento.dto.MetricasDaEmpresaResponse;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/acompanhamento")
@Tag(name = "Acompanhamento", description = "Leitura do progresso das empresas, restrita a conta NATY")
public class AcompanhamentoController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final AcompanhamentoService service;

    public AcompanhamentoController(AcompanhamentoService service) {
        this.service = service;
    }

    @GetMapping("/empresas/{empresaId}/integrantes")
    @Operation(
            summary = "Lista os integrantes da empresa com a situacao de cada um",
            description = "Situacoes PARADO, SEM_INICIO, AVANCANDO e CONCLUIU, nessa ordem, com mais dias sem concluir"
                    + " primeiro. O filtro situacao restringe a uma delas. Paginada por pagina e tamanho, com tamanho"
                    + " maximo de 100. Parado e o integrante que passou app.acompanhamento.dias-parado dias de calendario,"
                    + " no fuso da empresa, sem concluir atividade.")
    public PaginaResponse<IntegranteAcompanhadoResponse> listar(
            @PathVariable UUID empresaId,
            @RequestParam(required = false) SituacaoDeAcompanhamento situacao,
            @ParameterObject Pageable pagina) {
        return service.listar(empresaId, situacao, pagina);
    }

    @GetMapping("/empresas/{empresaId}/metricas")
    @Operation(
            summary = "Devolve adesao, percentual medio de conclusao e a contagem por situacao",
            description = "As contagens coincidem com as situacoes da listagem de integrantes.")
    public MetricasDaEmpresaResponse metricas(@PathVariable UUID empresaId) {
        return service.metricas(empresaId);
    }

    @GetMapping("/empresas/{empresaId}/integrantes.csv")
    @Operation(
            summary = "Exporta a lista de integrantes em CSV",
            description = "Mesma ordem e mesmo filtro situacao da listagem, sem paginacao. UTF-8 com BOM, separador"
                    + " virgula, e celula de texto que comeca com =, +, - ou @ vai com apostrofo no inicio.")
    public ResponseEntity<String> exportar(
            @PathVariable UUID empresaId, @RequestParam(required = false) SituacaoDeAcompanhamento situacao) {
        String csv = CsvDeAcompanhamento.gerar(service.todos(empresaId, situacao));
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("integrantes-" + empresaId + ".csv")
                                .build()
                                .toString())
                .body(csv);
    }

    @GetMapping("/atividades")
    @Operation(
            summary = "Mostra quantos chegaram e quantos passaram em cada atividade",
            description = "Chegaram sao os integrantes para quem a atividade esta liberada, passaram sao os que a"
                    + " concluiram. O filtro empresaId restringe a uma empresa, e sem ele soma todas.")
    public List<FunilDeAtividadeResponse> funil(@RequestParam(required = false) UUID empresaId) {
        return service.funil(empresaId);
    }
}
