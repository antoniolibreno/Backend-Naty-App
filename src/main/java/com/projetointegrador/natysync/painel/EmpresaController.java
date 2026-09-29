package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.painel.dto.EmpresaRequest;
import com.projetointegrador.natysync.painel.dto.EmpresaResponse;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/painel/empresas")
@Tag(name = "Painel: empresas", description = "Cadastro de empresas. Exige o papel NATY.")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    @Operation(
            summary = "Lista as empresas",
            description = "Paginada por pagina e tamanho, com tamanho maximo de 100. A ordem e sempre por nome,"
                    + " e o parametro de ordenacao e ignorado.")
    public PaginaResponse<EmpresaResponse> listar(@ParameterObject Pageable pagina) {
        return empresaService.listar(pagina);
    }

    @GetMapping("/{empresaId}")
    @Operation(summary = "Busca uma empresa pelo identificador")
    public EmpresaResponse buscarPorId(@PathVariable UUID empresaId) {
        return empresaService.buscarPorId(empresaId);
    }

    @PostMapping
    @Operation(
            summary = "Cria uma empresa",
            description = "O fuso horario e um identificador de regiao, como America/Sao_Paulo.")
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody EmpresaRequest requisicao) {
        EmpresaResponse resposta = empresaService.criar(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/painel/empresas/" + resposta.id()))
                .body(resposta);
    }

    @PutMapping("/{empresaId}")
    @Operation(
            summary = "Atualiza uma empresa",
            description = "Desativar a empresa revoga as sessoes dos integrantes dela. Empresa com conta NATY"
                    + " devolve 409 EMPRESA_COM_CONTA_NATY.")
    public EmpresaResponse atualizar(@PathVariable UUID empresaId, @Valid @RequestBody EmpresaRequest requisicao) {
        return empresaService.atualizar(empresaId, requisicao);
    }

    @DeleteMapping("/{empresaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Exclui uma empresa sem integrantes",
            description = "Empresa com integrante devolve 409 EMPRESA_COM_VINCULOS, e empresa com conta NATY"
                    + " devolve 409 EMPRESA_COM_CONTA_NATY. O caminho para empresa com historico e a desativacao.")
    public void excluir(@PathVariable UUID empresaId) {
        empresaService.excluir(empresaId);
    }
}
