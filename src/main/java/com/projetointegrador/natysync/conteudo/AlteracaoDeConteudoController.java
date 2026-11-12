package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlteracaoDeConteudoResponse;
import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alteracoes-de-conteudo")
@Tag(
        name = "Conteudo",
        description =
                "Escrita do conteudo global de treinamento: trilha, modulo, atividade, quiz, pergunta e alternativa."
                        + " Toda rota de escrita exige conta NATY. A ordem de criacao acrescenta ao fim, e conteudo com historico nao e removido."
                        + " Cada alteracao grava um registro de auditoria.")
public class AlteracaoDeConteudoController {

    private final AlteracaoDeConteudoService service;

    public AlteracaoDeConteudoController(AlteracaoDeConteudoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Lista quem alterou o conteudo e quando",
            description = "Registro de auditoria paginado, do mais recente para o mais antigo, com pagina "
                    + "e tamanho. O filtro entidadeId restringe a um item e continua valido depois da "
                    + "remocao dele. O parametro de ordenacao e ignorado. Exige conta NATY e vale "
                    + "para todas as empresas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina de registros de alteracao"),
        @ApiResponse(
                responseCode = "403",
                description = "Conta sem papel NATY. Codigo ACESSO_NEGADO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<AlteracaoDeConteudoResponse> listar(
            @Parameter(
                            description = "Restringe aos registros do item com este identificador, de qualquer tipo",
                            example = "c7a4e910-2b3d-4e56-8f01-6a9d3c5b7e22")
                    @RequestParam(required = false)
                    UUID entidadeId,
            @ParameterObject Pageable pagina) {
        return service.listar(entidadeId, pagina);
    }
}
