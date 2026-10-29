package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.PosicaoRankingResponse;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ranking")
@Tag(name = "Gamificacao", description = "Pontos, sequencia de dias, conquistas e ranking da empresa de quem chama")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    @Operation(
            summary = "Classifica as contas ativas da empresa de quem chama",
            description = "Ordem por pontos, desempate por quem chegou primeiro ao total e depois por nome. Paginada"
                    + " por pagina e tamanho, com tamanho maximo de 100, e o parametro de ordenacao e ignorado.")
    public PaginaResponse<PosicaoRankingResponse> listar(
            @ParameterObject Pageable pagina, IntegranteDaRequisicao quem) {
        return rankingService.listar(quem.empresaId(), pagina);
    }
}
