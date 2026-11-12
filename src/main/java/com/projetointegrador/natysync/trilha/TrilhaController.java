package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.trilha.dto.TrilhaDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.TrilhaResumoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trilhas")
@Tag(name = "Trilhas", description = "Conteúdo do treinamento, igual para todas as empresas")
public class TrilhaController {

    private final TrilhaService trilhaService;

    public TrilhaController(TrilhaService trilhaService) {
        this.trilhaService = trilhaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar as trilhas ativas",
            description = "Devolve as trilhas ativas em ordem crescente, sem módulos.")
    @ApiResponse(responseCode = "200", description = "Trilhas ativas")
    public List<TrilhaResumoResponse> listar() {
        return trilhaService.listarAtivas();
    }

    @GetMapping("/{trilhaId}")
    @Operation(
            summary = "Consultar uma trilha",
            description = "Devolve os módulos e as atividades em ordem crescente, sem progresso do integrante.")
    @ApiResponse(responseCode = "200", description = "Trilha com módulos")
    @ApiResponse(
            responseCode = "404",
            description = "Trilha não encontrada",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public TrilhaDetalheResponse detalhar(
            @Parameter(description = "Identificador da trilha", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @PathVariable
                    UUID trilhaId) {
        return trilhaService.buscarDetalhe(trilhaId);
    }
}
