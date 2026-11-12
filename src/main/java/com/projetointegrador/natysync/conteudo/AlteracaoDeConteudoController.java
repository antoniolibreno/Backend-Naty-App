package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlteracaoDeConteudoResponse;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alteracoes-de-conteudo")
@Tag(name = "Conteúdo", description = "Escrita do conteúdo global de treinamento. Exige conta NATY.")
public class AlteracaoDeConteudoController {

    private final AlteracaoDeConteudoService service;

    public AlteracaoDeConteudoController(AlteracaoDeConteudoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Listar alterações de conteúdo",
            description = "Auditoria paginada, da mais recente para a mais antiga. Exige conta NATY.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de alterações"),
        @ApiResponse(
                responseCode = "403",
                description = "Papel NATY exigido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<AlteracaoDeConteudoResponse> listar(
            @Parameter(description = "Filtra pelo item alterado", example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @RequestParam(required = false)
                    UUID entidadeId,
            @ParameterObject Pageable pagina) {
        return service.listar(entidadeId, pagina);
    }
}
