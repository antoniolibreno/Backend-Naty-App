package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlternativaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.AlternativaCriacaoRequest;
import com.projetointegrador.natysync.conteudo.dto.AlternativaRequest;
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
@RequestMapping("/api/v1/alternativas")
@Tag(name = "Conteudo", description = "Escrita do conteudo de treinamento, restrita a conta NATY")
public class AlternativaConteudoController {

    private final QuizConteudoService quizService;

    public AlternativaConteudoController(QuizConteudoService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cria uma alternativa no fim da pergunta",
            description = "Com correta verdadeiro, a marca de correta se move da alternativa anterior para a nova.")
    public AlternativaAdminResponse criar(
            @Valid @RequestBody AlternativaCriacaoRequest requisicao, IntegranteDaRequisicao quem) {
        return quizService.criarAlternativa(requisicao, quem.usuarioId());
    }

    @PutMapping("/{alternativaId}")
    @Operation(
            summary = "Altera o texto e a marca de correta da alternativa",
            description = "Com correta verdadeiro, a marca se move. Desmarcar a unica correta responde 409"
                    + " GABARITO_INVALIDO.")
    public AlternativaAdminResponse alterar(
            @PathVariable UUID alternativaId,
            @Valid @RequestBody AlternativaRequest requisicao,
            IntegranteDaRequisicao quem) {
        return quizService.alterarAlternativa(alternativaId, requisicao, quem.usuarioId());
    }

    @DeleteMapping("/{alternativaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove a alternativa",
            description = "Responde 409 GABARITO_INVALIDO para a correta ou quando restariam menos de duas, e 409"
                    + " CONTEUDO_COM_PROGRESSO quando alguma tentativa a escolheu.")
    public void remover(@PathVariable UUID alternativaId, IntegranteDaRequisicao quem) {
        quizService.removerAlternativa(alternativaId, quem.usuarioId());
    }
}
