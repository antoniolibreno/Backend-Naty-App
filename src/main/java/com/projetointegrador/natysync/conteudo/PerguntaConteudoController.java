package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.PerguntaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.PerguntaAlteracaoRequest;
import com.projetointegrador.natysync.conteudo.dto.PerguntaCriacaoRequest;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perguntas")
@Tag(name = "Conteudo", description = "Escrita do conteudo de treinamento, restrita a conta NATY")
public class PerguntaConteudoController {

    private final QuizConteudoService quizService;

    public PerguntaConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma pergunta no fim do quiz",
            description = "Ao menos duas alternativas e exatamente uma correta.")
    public PerguntaAdminResponse criar(
            @Valid @RequestBody PerguntaCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarPergunta(requisicao, quem.usuarioId());
    }

    @PutMapping("/{perguntaId}")
    @Operation(summary = "Altera o enunciado da pergunta")
    public PerguntaAdminResponse alterar(
            @PathVariable UUID perguntaId,
            @Valid @RequestBody PerguntaAlteracaoRequest requisicao,
            IntegranteDaRequisicao quem) {
        return quizService.alterarPergunta(perguntaId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{perguntaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove a pergunta com suas alternativas",
            description = "Responde 409 QUIZ_SEM_PERGUNTA para a ultima pergunta do quiz e 409"
                    + " CONTEUDO_COM_PROGRESSO quando alguma tentativa respondeu a pergunta.")
    public void remover(@PathVariable UUID perguntaId, IntegranteDaRequisicao quem) {
        quizService.removerPergunta(perguntaId, quem.usuarioId());
    }
}
