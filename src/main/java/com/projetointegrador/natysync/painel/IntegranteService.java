package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.painel.dto.IntegranteAlteracaoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteCriacaoRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteFiltro;
import com.projetointegrador.natysync.painel.dto.IntegranteResponse;
import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.IntegranteDaRequisicao;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.SessaoService;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IntegranteService {

    private static final Sort ORDEM_DA_LISTAGEM = Sort.by("nome", "id");
    private static final String STATUS_INICIAL = "offline";

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final SessaoService sessaoService;
    private final PasswordEncoder codificadorDeSenha;
    private final IntegranteMapper integranteMapper;

    public IntegranteService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            SessaoService sessaoService,
            PasswordEncoder codificadorDeSenha,
            IntegranteMapper integranteMapper) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.sessaoService = sessaoService;
        this.codificadorDeSenha = codificadorDeSenha;
        this.integranteMapper = integranteMapper;
    }

    public PaginaResponse<IntegranteResponse> listar(UUID empresaId, IntegranteFiltro filtro, Pageable pagina) {
        buscarEmpresa(empresaId);
        PageRequest pedido = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), ORDEM_DA_LISTAGEM);
        return PaginaResponse.de(
                usuarioRepository.findAll(daEmpresa(empresaId, filtro), pedido), integranteMapper::paraResposta);
    }

    public IntegranteResponse buscar(UUID empresaId, UUID integranteId) {
        return integranteMapper.paraResposta(buscarIntegrante(empresaId, integranteId));
    }

    @Transactional
    public IntegranteResponse criar(UUID empresaId, IntegranteCriacaoRequest requisicao) {
        Empresa empresa = buscarEmpresa(empresaId);
        recusarEmailRepetido(usuarioRepository.existeEmailNormalizado(requisicao.email()));

        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNome(requisicao.nome());
        usuario.setEmail(requisicao.email());
        usuario.setPapel(requisicao.papel());
        usuario.setPerfil(requisicao.perfil());
        usuario.setStatus(STATUS_INICIAL);
        usuario.setSenhaHash(codificadorDeSenha.encode(requisicao.senha()));
        return integranteMapper.paraResposta(salvar(usuario));
    }

    @Transactional
    public IntegranteResponse alterar(
            UUID empresaId, UUID integranteId, IntegranteAlteracaoRequest requisicao, IntegranteDaRequisicao quem) {
        Usuario usuario = buscarIntegranteAdministravel(empresaId, integranteId);
        if (usuario.getId().equals(quem.usuarioId()) && usuario.getPapel() != requisicao.papel()) {
            throw operacaoNaPropriaConta("O proprio papel nao pode ser alterado.");
        }
        recusarEmailRepetido(usuarioRepository.existeEmailNormalizadoEmOutro(requisicao.email(), integranteId));

        usuario.setNome(requisicao.nome());
        usuario.setEmail(requisicao.email());
        usuario.setPapel(requisicao.papel());
        usuario.setPerfil(requisicao.perfil());
        return integranteMapper.paraResposta(salvar(usuario));
    }

    @Transactional
    public void definirSenha(UUID empresaId, UUID integranteId, String senha) {
        Usuario usuario = buscarIntegranteAdministravel(empresaId, integranteId);
        usuario.setSenhaHash(codificadorDeSenha.encode(senha));
        salvar(usuario);
        sessaoService.revogarTodasDoIntegrante(integranteId);
    }

    @Transactional
    public void definirAtivo(UUID empresaId, UUID integranteId, boolean ativo, IntegranteDaRequisicao quem) {
        Usuario usuario = buscarIntegranteAdministravel(empresaId, integranteId);
        if (!ativo && usuario.getId().equals(quem.usuarioId())) {
            throw operacaoNaPropriaConta("A propria conta nao pode ser desativada.");
        }
        usuario.setAtivo(ativo);
        salvar(usuario);
        if (!ativo) {
            sessaoService.revogarTodasDoIntegrante(integranteId);
        }
    }

    private Usuario salvar(Usuario usuario) {
        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException excecao) {
            throw new ConflitoException(CodigoDeConflito.EMAIL_JA_CADASTRADO, "E-mail ja cadastrado.");
        }
    }

    private void recusarEmailRepetido(boolean emailExiste) {
        if (emailExiste) {
            throw new ConflitoException(CodigoDeConflito.EMAIL_JA_CADASTRADO, "E-mail ja cadastrado.");
        }
    }

    private ConflitoException operacaoNaPropriaConta(String mensagem) {
        return new ConflitoException(CodigoDeConflito.OPERACAO_NA_PROPRIA_CONTA, mensagem);
    }

    private Usuario buscarIntegranteAdministravel(UUID empresaId, UUID integranteId) {
        Usuario usuario = buscarIntegrante(empresaId, integranteId);
        if (usuario.getPapel() == Papel.NATY) {
            throw new ConflitoException(
                    CodigoDeConflito.CONTA_NATY_FORA_DO_PAINEL, "Conta NATY nao e administrada pelo painel.");
        }
        return usuario;
    }

    private Usuario buscarIntegrante(UUID empresaId, UUID integranteId) {
        return usuarioRepository
                .buscarNaEmpresa(integranteId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Integrante nao encontrado: " + integranteId));
    }

    private Empresa buscarEmpresa(UUID empresaId) {
        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada: " + empresaId));
    }

    private static Specification<Usuario> daEmpresa(UUID empresaId, IntegranteFiltro filtro) {
        return (raiz, consulta, criterio) -> {
            var condicao = criterio.equal(raiz.get("empresa").get("id"), empresaId);
            if (filtro.ativo() != null) {
                condicao = criterio.and(condicao, criterio.equal(raiz.get("ativo"), filtro.ativo()));
            }
            if (filtro.busca() != null) {
                String padrao = "%" + escaparCuringa(filtro.busca()) + "%";
                condicao = criterio.and(
                        condicao,
                        criterio.or(
                                criterio.like(criterio.lower(raiz.get("nome")), padrao, '\\'),
                                criterio.like(criterio.lower(raiz.get("email")), padrao, '\\')));
            }
            return condicao;
        };
    }

    private static String escaparCuringa(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
