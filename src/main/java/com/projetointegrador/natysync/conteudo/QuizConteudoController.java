package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.QuizAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.QuizAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.QuizCriacaoRequest;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quizzes")
@Tag(name = "Conteudo", description = "Escrita do conteudo de treinamento, restrita a conta NATY")
public class QuizConteudoController {

    private final QuizConteudoService quizService;

    public QuizConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{quizId}")
    @Operation(
            summary = "Le o quiz com o gabarito",
            description = "Unico canal que devolve o campo correta. Exige conta NATY. A leitura do integrante em"
                    + " /api/v1/atividades/{id}/quiz nao tem o campo.")
    public QuizAdminResponse buscar(@PathVariable UUID quizId) {
        return quizService.buscar(quizId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria o quiz de uma atividade com suas perguntas e alternativas",
            description = "Nota minima de 0 a 100, padrao 70. Cada pergunta tem ao menos duas alternativas e"
                    + " exatamente uma correta. Atividade que ja tem quiz responde 409 QUIZ_JA_EXISTE.")
    public QuizAdminResponse criar(@Valid @RequestBody QuizCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarQuiz(requisicao, quem.usuarioId());
    }

    @PutMapping("/{quizId}")
    @Operation(summary = "Altera a nota minima do quiz")
    public QuizAdminResponse alterar(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return quizService.alterarQuiz(quizId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove o quiz com suas perguntas e alternativas",
            description = "Responde 409 CONTEUDO_COM_PROGRESSO quando a atividade tem progresso, tentativa ou ponto.")
    public void remover(@PathVariable UUID quizId, IntegranteDaRequisicao quem) {
        quizService.removerQuiz(quizId, quem.usuarioId());
    }
}
