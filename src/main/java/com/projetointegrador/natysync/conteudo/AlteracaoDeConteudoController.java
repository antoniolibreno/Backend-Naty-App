package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlteracaoDeConteudoResponse;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Conteudo", description = "Escrita do conteudo de treinamento, restrita a conta NATY")
public class AlteracaoDeConteudoController {

    private final AlteracaoDeConteudoService service;

    public AlteracaoDeConteudoController(AlteracaoDeConteudoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Lista quem alterou o conteudo e quando",
            description = "Do mais recente para o mais antigo, paginada por pagina e tamanho. O filtro entidadeId"
                    + " restringe a um item. O parametro de ordenacao e ignorado.")
    public PaginaResponse<AlteracaoDeConteudoResponse> listar(
            @RequestParam(required = false) UUID entidadeId, @ParameterObject Pageable pagina) {
        return service.listar(entidadeId, pagina);
    }
}
