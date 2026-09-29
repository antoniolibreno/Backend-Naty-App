package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.painel.dto.EmpresaRequest;
import com.projetointegrador.natysync.painel.dto.EmpresaResponse;
import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import com.projetointegrador.natysync.usuario.SessaoService;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.util.UUID;
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
    public EmpresaResponse atualizar(UUID empresaId, EmpresaRequest requisicao, IntegranteDaRequisicao quem) {
        Empresa empresa = buscarEmpresa(empresaId);
        boolean desativando = empresa.isAtiva() && !requisicao.ativa();
        if (desativando) {
            recusarSeForAPropria(empresaId, quem);
        }
        preencher(empresa, requisicao);
        Empresa salva = empresaRepository.saveAndFlush(empresa);
        if (desativando) {
            sessaoService.revogarTodasDaEmpresa(empresaId);
        }
        return empresaMapper.paraResposta(salva);
    }

    @Transactional
    public void excluir(UUID empresaId, IntegranteDaRequisicao quem) {
        Empresa empresa = buscarEmpresa(empresaId);
        recusarSeForAPropria(empresaId, quem);
        if (usuarioRepository.existsByEmpresaId(empresaId)) {
            throw new ConflitoException(
                    CodigoDeConflito.EMPRESA_COM_VINCULOS, "Empresa possui integrantes e nao pode ser excluida.");
        }
        empresaRepository.delete(empresa);
    }

    private void preencher(Empresa empresa, EmpresaRequest requisicao) {
        empresa.setNome(requisicao.nome());
        empresa.setAtiva(requisicao.ativa());
        empresa.setFusoHorario(requisicao.fuso());
    }

    private void recusarSeForAPropria(UUID empresaId, IntegranteDaRequisicao quem) {
        if (empresaId.equals(quem.empresaId())) {
            throw new ConflitoException(
                    CodigoDeConflito.OPERACAO_NA_PROPRIA_CONTA,
                    "A empresa de quem chama nao pode ser desativada nem excluida.");
        }
    }

    private Empresa buscarEmpresa(UUID empresaId) {
        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada: " + empresaId));
    }
}
