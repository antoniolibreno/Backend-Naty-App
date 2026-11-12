package com.projetointegrador.natysync.gamificacao;

import com.projetointegrador.natysync.gamificacao.dto.GamificacaoResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrantes")
@Tag(name = "Gamificacao", description = "Pontos, sequencia de dias, conquistas e ranking da empresa de quem chama")
public class GamificacaoController {

    private final GamificacaoService gamificacaoService;

    public GamificacaoController(GamificacaoService gamificacaoService) {
        this.gamificacaoService = gamificacaoService;
    }

    @GetMapping("/{integranteId}/gamificacao")
    @Operation(
            summary = "Devolve pontos, posicao no ranking, sequencia de dias e conquistas do integrante",
            description = "Le a propria conta ou a de um colega ativo da mesma empresa. Outra empresa, conta"
                    + " desativada e identificador inexistente respondem 404.")
    public GamificacaoResponse buscar(@PathVariable UUID integranteId, IntegranteDaRequisicao quem) {
        return gamificacaoService.buscar(integranteId, quem.empresaId());
    }
}
