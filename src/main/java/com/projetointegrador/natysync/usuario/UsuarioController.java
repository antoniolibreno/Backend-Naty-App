package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Leitura dos integrantes da empresa de quem chama")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(
            summary = "Lista os integrantes ativos da empresa de quem chama",
            description = "Devolve identificador, nome e papel, ordenados por nome. Paginada por pagina e tamanho,"
                    + " com tamanho maximo de 100, e o parametro de ordenacao e ignorado.")
    public PaginaResponse<UsuarioResponse> listar(@ParameterObject Pageable pagina, IntegranteDaRequisicao quem) {
        return usuarioService.listarAtivosDaEmpresa(quem.empresaId(), pagina);
    }
}
