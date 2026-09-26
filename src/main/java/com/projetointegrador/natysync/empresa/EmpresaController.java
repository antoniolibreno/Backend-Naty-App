package com.projetointegrador.natysync.empresa;

import com.projetointegrador.natysync.empresa.dto.EmpresaRequest;
import com.projetointegrador.natysync.empresa.dto.EmpresaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/empresas")
@Tag(name = "Empresas", description = "Cadastro de empresas")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    @Operation(summary = "Lista as empresas")
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{empresaId}")
    @Operation(summary = "Busca uma empresa pelo ID")
    public EmpresaResponse buscarPorId(@PathVariable UUID empresaId) {
        return empresaService.buscarPorId(empresaId);
    }

    @PostMapping
    @Operation(summary = "Cria uma empresa")
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody EmpresaRequest requisicao) {
        EmpresaResponse resposta = empresaService.criar(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/empresas/" + resposta.id())).body(resposta);
    }

    @PutMapping("/{empresaId}")
    @Operation(summary = "Atualiza uma empresa")
    public EmpresaResponse atualizar(@PathVariable UUID empresaId, @Valid @RequestBody EmpresaRequest requisicao) {
        return empresaService.atualizar(empresaId, requisicao);
    }

    @DeleteMapping("/{empresaId}")
    @Operation(summary = "Exclui uma empresa")
    public ResponseEntity<Void> excluir(@PathVariable UUID empresaId) {
        empresaService.excluir(empresaId);
        return ResponseEntity.noContent().build();
    }
}
