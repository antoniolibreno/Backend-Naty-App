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
@Tag(
        name = "Usuarios",
        description =
                "Leitura dos colegas da empresa de quem chama. Qualquer papel autenticado acessa, e o e-mail nunca sai")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(
            summary = "Lista os integrantes ativos da empresa de quem chama",
            description = "Exige sessao valida, de qualquer papel. Devolve identificador, nome e papel dos integrantes"
                    + " ativos da empresa de quem chama, ordenados por nome e identificador. Paginada por pagina e"
                    + " tamanho, com tamanho maximo de 100, e o parametro de ordenacao e ignorado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagina de integrantes ativos da empresa"),
        @ApiResponse(
                responseCode = "400",
                description = "Parametro de consulta invalido. Codigo FALHA_DE_VALIDACAO",
                content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public PaginaResponse<UsuarioResponse> listar(@ParameterObject Pageable pagina, IntegranteDaRequisicao quem) {
        return usuarioService.listarAtivosDaEmpresa(quem.empresaId(), pagina);
    }
}
