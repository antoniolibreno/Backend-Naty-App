package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.PosicaoRankingResponse;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ranking")
@Tag(
        name = "Gamificação",
        description = "Pontos, sequência, conquistas e ranking da própria empresa. Exige sessão válida.")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    @Operation(
            summary = "Listar o ranking da empresa",
            description = "Classifica por pontos as contas ativas da empresa de quem chama, de qualquer papel."
                    + " Paginada, com até 100 itens por página.")
    @ApiResponse(responseCode = "200", description = "Página do ranking")
    @ApiResponse(
            responseCode = "400",
            description = "Paginação inválida",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public PaginaResponse<PosicaoRankingResponse> listar(
            @ParameterObject Pageable pagina, IntegranteDaRequisicao quem) {
        return rankingService.listar(quem.empresaId(), pagina);
    }
}
