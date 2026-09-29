package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.painel.dto.EmpresaRequest;
import com.projetointegrador.natysync.painel.dto.EmpresaResponse;
import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.SessaoService;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmpresaService {

    private static final Sort ORDEM_DA_LISTAGEM = Sort.by("nome", "id");

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SessaoService sessaoService;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            SessaoService sessaoService,
            EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.sessaoService = sessaoService;
        this.empresaMapper = empresaMapper;
    }

    public PaginaResponse<EmpresaResponse> listar(Pageable pagina) {
        PageRequest pedido = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), ORDEM_DA_LISTAGEM);
        return PaginaResponse.de(empresaRepository.findAll(pedido), empresaMapper::paraResposta);
    }

    public EmpresaResponse buscarPorId(UUID empresaId) {
        return empresaMapper.paraResposta(buscarEmpresa(empresaId));
    }

    @Transactional
    public EmpresaResponse criar(EmpresaRequest requisicao) {
        Empresa empresa = new Empresa();
        preencher(empresa, requisicao);
        return empresaMapper.paraResposta(empresaRepository.saveAndFlush(empresa));
    }

    @Transactional
    public EmpresaResponse atualizar(UUID empresaId, EmpresaRequest requisicao) {
        Empresa empresa = buscarEmpresa(empresaId);
        boolean desativando = empresa.isAtiva() && !requisicao.ativa();
        if (desativando) {
            recusarSeTiverContaNaty(empresaId);
            usuarioRepository.travarTodosDaEmpresa(empresaId);
        }
        preencher(empresa, requisicao);
        Empresa salva = empresaRepository.saveAndFlush(empresa);
        if (desativando) {
            sessaoService.revogarTodasDaEmpresa(empresaId);
        }
        return empresaMapper.paraResposta(salva);
    }

    @Transactional
    public void excluir(UUID empresaId) {
        Empresa empresa = buscarEmpresa(empresaId);
        recusarSeTiverContaNaty(empresaId);
        if (usuarioRepository.existsByEmpresaId(empresaId)) {
            throw empresaComVinculos();
        }
        try {
            empresaRepository.delete(empresa);
            empresaRepository.flush();
        } catch (DataIntegrityViolationException excecao) {
            throw empresaComVinculos();
        }
    }

    private void preencher(Empresa empresa, EmpresaRequest requisicao) {
        empresa.setNome(requisicao.nome());
        empresa.setAtiva(requisicao.ativa());
        empresa.setFusoHorario(requisicao.fuso());
    }

    private void recusarSeTiverContaNaty(UUID empresaId) {
        if (usuarioRepository.existsByEmpresaIdAndPapel(empresaId, Papel.NATY)) {
            throw new ConflitoException(
                    CodigoDeConflito.EMPRESA_COM_CONTA_NATY,
                    "Empresa com conta NATY nao pode ser desativada nem excluida.");
        }
    }

    private ConflitoException empresaComVinculos() {
        return new ConflitoException(
                CodigoDeConflito.EMPRESA_COM_VINCULOS, "Empresa possui registros vinculados e nao pode ser excluida.");
    }

    private Empresa buscarEmpresa(UUID empresaId) {
        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada: " + empresaId));
    }
}
