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
        name = "Gamificacao",
        description = "Pontos, sequencia de dias de estudo, conquistas e ranking. Tudo e filtrado pela empresa de quem"
                + " chama, e nao existe ranking entre empresas. Exige sessao valida, sem papel especifico")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    @Operation(
            summary = "Classifica as contas ativas da empresa de quem chama",
            description = "Exige sessao valida de qualquer papel. Classifica todas as contas ativas da empresa de quem"
                    + " chama, de qualquer papel e inclusive sem ponto. Ordem por pontos decrescentes, depois por"
                    + " quem chegou primeiro ao total e depois por nome. A posicao e ordinal e nao se repete."
                    + " Paginada por pagina e tamanho, com tamanho maximo de 100, e o parametro de ordenacao e"
                    + " ignorado. Somente leitura.")
    @ApiResponse(responseCode = "200", description = "Pagina do ranking da empresa de quem chama")
    @ApiResponse(
            responseCode = "400",
            description = "Parametro de paginacao invalido. Codigo FALHA_DE_VALIDACAO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public PaginaResponse<PosicaoRankingResponse> listar(
            @ParameterObject Pageable pagina, IntegranteDaRequisicao quem) {
        return rankingService.listar(quem.empresaId(), pagina);
    }
}
