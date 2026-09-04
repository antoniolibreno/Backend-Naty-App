package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.QuizResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/atividades")
@Tag(name = "Atividades", description = "Atividades da trilha e seus quizzes")
public class AtividadeController {

    private final TrilhaService trilhaService;

    public AtividadeController(TrilhaService trilhaService) {
        this.trilhaService = trilhaService;
    }

    @GetMapping("/{atividadeId}")
    @Operation(summary = "Detalha uma atividade")
    public AtividadeDetalheResponse detalhar(@PathVariable UUID atividadeId) {
        return trilhaService.buscarAtividade(atividadeId);
    }

    @GetMapping("/{atividadeId}/quiz")
    @Operation(summary = "Devolve o quiz da atividade, sem a indicacao de alternativa correta")
    public QuizResponse buscarQuiz(@PathVariable UUID atividadeId) {
        return trilhaService.buscarQuizDaAtividade(atividadeId);
    }
}
