package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.GamificacaoResponse;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrantes")
@Tag(
        name = "Gamificação",
        description = "Pontos, sequência, conquistas e ranking da própria empresa. Exige sessão válida.")
public class GamificacaoController {

    private final GamificacaoService gamificacaoService;

    public GamificacaoController(GamificacaoService gamificacaoService) {
        this.gamificacaoService = gamificacaoService;
    }

    @GetMapping("/{integranteId}/gamificacao")
    @Operation(
            summary = "Buscar a gamificação do integrante",
            description = "Devolve pontos, posição no ranking, sequência e conquistas. Vale para a própria conta ou"
                    + " para colega ativo da mesma empresa.")
    @ApiResponse(responseCode = "200", description = "Perfil de gamificação")
    @ApiResponse(
            responseCode = "400",
            description = "Identificador malformado",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Integrante inexistente, inativo ou de outra empresa",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public GamificacaoResponse buscar(
            @Parameter(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
                    @PathVariable
                    UUID integranteId,
            IntegranteDaRequisicao quem) {
        return gamificacaoService.buscar(integranteId, quem.empresaId());
    }
}
