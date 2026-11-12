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
        name = "Gamificacao",
        description = "Pontos, sequencia de dias de estudo, conquistas e ranking. Tudo e filtrado pela empresa de quem"
                + " chama, e nao existe ranking entre empresas. Exige sessao valida, sem papel especifico")
public class GamificacaoController {

    private final GamificacaoService gamificacaoService;

    public GamificacaoController(GamificacaoService gamificacaoService) {
        this.gamificacaoService = gamificacaoService;
    }

    @GetMapping("/{integranteId}/gamificacao")
    @Operation(
            summary = "Devolve pontos, posicao no ranking, sequencia de dias e conquistas do integrante",
            description = "Exige sessao valida de qualquer papel. Le a propria conta ou a de um colega ativo da"
                    + " mesma empresa. Pontos, sequencia e posicao sao calculados a cada leitura. Outra empresa,"
                    + " conta desativada e identificador inexistente respondem o mesmo 404, para nao confirmar"
                    + " identificador. Somente leitura.")
    @ApiResponse(responseCode = "200", description = "Perfil de gamificacao do integrante")
    @ApiResponse(
            responseCode = "400",
            description = "Identificador do integrante malformado. Codigo FALHA_DE_VALIDACAO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Integrante inexistente, desativado ou de outra empresa. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public GamificacaoResponse buscar(
            @Parameter(description = "Identificador do integrante", example = "9b2d7e41-3c5a-4f68-a1d9-7c0e5b8f2a34")
                    @PathVariable
                    UUID integranteId,
            IntegranteDaRequisicao quem) {
        return gamificacaoService.buscar(integranteId, quem.empresaId());
    }
}
