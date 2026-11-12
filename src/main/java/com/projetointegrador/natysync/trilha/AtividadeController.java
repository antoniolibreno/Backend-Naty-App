package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.QuizResponse;
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
@RequestMapping("/api/v1/atividades")
@Tag(
        name = "Atividades",
        description =
                "Leitura de atividade e do quiz dela. O quiz nunca traz a alternativa correta, e a correcao acontece em POST /api/v1/atividades/{atividadeId}/quiz/tentativas")
public class AtividadeController {

    private final TrilhaService trilhaService;

    public AtividadeController(TrilhaService trilhaService) {
        this.trilhaService = trilhaService;
    }

    @GetMapping("/{atividadeId}")
    @Operation(
            summary = "Detalha uma atividade",
            description = "Devolve imagem, video, duracao, pontos e se a atividade tem quiz. Nao informa o estado de"
                    + " desbloqueio do integrante. Atividade sem video tem videoUrl nulo.")
    @ApiResponse(responseCode = "200", description = "Atividade encontrada")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade nao encontrada. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public AtividadeDetalheResponse detalhar(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId) {
        return trilhaService.buscarAtividade(atividadeId);
    }

    @GetMapping("/{atividadeId}/quiz")
    @Operation(
            summary = "Devolve o quiz da atividade, sem a indicacao de alternativa correta",
            description = "Devolve a nota minima, as perguntas e as alternativas em ordem crescente. O gabarito nao"
                    + " faz parte da resposta: a correcao e feita no servidor ao registrar a tentativa.")
    @ApiResponse(responseCode = "200", description = "Quiz da atividade")
    @ApiResponse(
            responseCode = "404",
            description = "Atividade nao encontrada ou atividade sem quiz. Codigo RECURSO_NAO_ENCONTRADO",
            content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    public QuizResponse buscarQuiz(
            @Parameter(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
                    @PathVariable
                    UUID atividadeId) {
        return trilhaService.buscarQuizDaAtividade(atividadeId);
    }
}
