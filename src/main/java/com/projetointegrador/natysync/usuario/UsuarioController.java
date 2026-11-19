package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Colegas da empresa de quem chama, para qualquer papel autenticado.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(
            summary = "Listar os colegas da empresa",
            description =
                    "Devolve identificador, nome e papel dos integrantes ativos da empresa, sem e-mail. Paginada, no máximo 100 por página, ordenada por nome.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de integrantes ativos"),
        @ApiResponse(
                responseCode = "400",
                description = "Parâmetro de paginação ou de filtro inválido",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<UsuarioResponse> listar(@ParameterObject Pageable pagina, IntegranteDaRequisicao quem) {
        return usuarioService.listarAtivosDaEmpresa(quem.empresaId(), pagina);
    }
}
