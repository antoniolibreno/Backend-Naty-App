package com.projetointegrador.natysync.empresa;

import com.projetointegrador.natysync.empresa.dto.EmpresaRequest;
import com.projetointegrador.natysync.empresa.dto.EmpresaResponse;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class EmpresaService {
    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(EmpresaRepository empresaRepository, EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.empresaMapper = empresaMapper;
    }

    public List<EmpresaResponse> listar() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::paraResposta)
                .toList();
    }

    public EmpresaResponse buscarPorId(UUID empresaId) {
        return empresaMapper.paraResposta(buscarEmpresa(empresaId));
    }

    @Transactional
    public EmpresaResponse criar(EmpresaRequest requisicao) {
        Empresa empresa = new Empresa();
        empresa.setNome(requisicao.nome());
        empresa.setAtiva(requisicao.ativa());
        return empresaMapper.paraResposta(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponse atualizar(UUID empresaId, EmpresaRequest requisicao) {
        Empresa empresa = buscarEmpresa(empresaId);
        empresa.setNome(requisicao.nome());
        empresa.setAtiva(requisicao.ativa());
        return empresaMapper.paraResposta(empresaRepository.save(empresa));
    }

    @Transactional
    public void excluir(UUID empresaId) {
        Empresa empresa = buscarEmpresa(empresaId);
        try {
            empresaRepository.delete(empresa);
            empresaRepository.flush();
        } catch (DataIntegrityViolationException excecao) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Empresa possui registros vinculados e nao pode ser excluida", excecao);
        }
    }

    private Empresa buscarEmpresa(UUID empresaId) {
        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada: " + empresaId));
    }
}
