package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.GamificacaoResponse;
import com.projetointegrador.natysync.gamificacao.dto.RankingItemResponse;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Gamificacao", description = "XP, niveis, sequencia, conquistas e ranking")
public class GamificacaoController {

    private final GamificacaoService service;

    public GamificacaoController(GamificacaoService service) {
        this.service = service;
    }

    @GetMapping("/integrantes/{integranteId}/gamificacao")
    @Operation(summary = "Busca o resumo de gamificacao de um integrante da empresa")
    public GamificacaoResponse buscar(@PathVariable UUID integranteId, IntegranteDaRequisicao quem) {
        return service.buscar(integranteId, quem);
    }

    @GetMapping("/ranking")
    @Operation(summary = "Lista o ranking paginado da empresa do usuario autenticado")
    public PaginaResponse<RankingItemResponse> ranking(
            @ParameterObject Pageable pageable, IntegranteDaRequisicao quem) {
        return service.ranking(quem.empresaId(), pageable);
    }
}
