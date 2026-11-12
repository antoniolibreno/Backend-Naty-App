package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.painel.dto.AdministradorInicialRequest;
import com.projetointegrador.natysync.painel.dto.EmpresaCriacaoRequest;
import com.projetointegrador.natysync.painel.dto.EmpresaRequest;
import com.projetointegrador.natysync.painel.dto.IntegranteCriacaoRequest;
import com.projetointegrador.natysync.shared.exception.ConflitoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.SessaoService;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;

class EmpresaServiceTest {

    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final SessaoService sessaoService = mock(SessaoService.class);
    private final IntegranteService integranteService = mock(IntegranteService.class);
    private final EmpresaService service = new EmpresaService(
            empresaRepository,
            usuarioRepository,
            sessaoService,
            Mappers.getMapper(EmpresaMapper.class),
            integranteService);

    private Empresa empresaAtiva() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setNome("Empresa");
        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(empresaRepository.saveAndFlush(any(Empresa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        return empresa;
    }

    @Test
    void criarNormalizaONomeEGuardaOFuso() {
        when(empresaRepository.saveAndFlush(any(Empresa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = service.criar(novaEmpresa());

        assertThat(resposta.nome()).isEqualTo("Empresa Teste");
        assertThat(resposta.fusoHorario()).isEqualTo("America/Manaus");
    }

    private EmpresaCriacaoRequest novaEmpresa() {
        return new EmpresaCriacaoRequest(
                " Empresa Teste ",
                true,
                "America/Manaus",
                CorpoDeEmpresa.cnpjNovo(),
                "Empresa Teste Ltda",
                new AdministradorInicialRequest("Carla", "Carla@Acme.com.br", "senha-nova-123", "(11) 98765-4321"));
    }

    @Test
    void criarCadastraOPrimeiroAdminDaEmpresaNova() {
        when(empresaRepository.saveAndFlush(any(Empresa.class))).thenAnswer(invocacao -> {
            Empresa salva = invocacao.getArgument(0);
            salva.setId(UUID.randomUUID());
            return salva;
        });

        var resposta = service.criar(novaEmpresa());

        var conta = org.mockito.ArgumentCaptor.forClass(IntegranteCriacaoRequest.class);
        verify(integranteService).criar(eq(resposta.id()), conta.capture());
        assertThat(conta.getValue().papel()).isEqualTo(Papel.ADMIN);
        assertThat(conta.getValue().email()).isEqualTo("carla@acme.com.br");
        assertThat(conta.getValue().telefone()).isEqualTo("11987654321");
    }

    @Test
    void emailRepetidoDoAdministradorPropagaParaDesfazerATransacao() {
        when(empresaRepository.saveAndFlush(any(Empresa.class))).thenAnswer(invocacao -> {
            Empresa salva = invocacao.getArgument(0);
            salva.setId(UUID.randomUUID());
            return salva;
        });
        when(integranteService.criar(any(), any()))
                .thenThrow(new ConflitoException("EMAIL_JA_CADASTRADO", "E-mail ja cadastrado."));

        assertThatThrownBy(() -> service.criar(novaEmpresa()))
                .isInstanceOfSatisfying(ConflitoException.class, excecao -> assertThat(excecao.getCodigo())
                        .isEqualTo("EMAIL_JA_CADASTRADO"));
    }

    @Test
    void desativarRevogaAsSessoesDaEmpresa() {
        Empresa empresa = empresaAtiva();

        service.atualizar(
                empresa.getId(),
                new EmpresaRequest("Empresa", false, "America/Sao_Paulo", CorpoDeEmpresa.cnpjNovo(), "Empresa Ltda"));

        verify(usuarioRepository).travarTodosDaEmpresa(empresa.getId());
        verify(sessaoService).revogarTodasDaEmpresa(empresa.getId());
        assertThat(empresa.getFusoHorario()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
    }

    @Test
    void alterarSemDesativarNaoRevogaSessao() {
        Empresa empresa = empresaAtiva();

        service.atualizar(
                empresa.getId(),
                new EmpresaRequest("Outro nome", true, "America/Sao_Paulo", CorpoDeEmpresa.cnpjNovo(), "Empresa Ltda"));

        verify(sessaoService, never()).revogarTodasDaEmpresa(any());
    }

    @Test
    void operacoesComEmpresaInexistenteRetornamErro() {
        UUID id = UUID.randomUUID();
        when(empresaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(id)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.excluir(id)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluirEmpresaComIntegranteRetornaConflito() {
        Empresa empresa = empresaAtiva();
        when(usuarioRepository.existsByEmpresaId(empresa.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(empresa.getId()))
                .isInstanceOfSatisfying(ConflitoException.class, excecao -> assertThat(excecao.getCodigo())
                        .isEqualTo("EMPRESA_COM_VINCULOS"));
        verify(empresaRepository, never()).delete(any());
    }

    @Test
    void empresaComContaNatyNaoEhDesativada() {
        Empresa empresa = empresaAtiva();
        when(usuarioRepository.existsByEmpresaIdAndPapel(empresa.getId(), Papel.NATY))
                .thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(
                        empresa.getId(),
                        new EmpresaRequest(
                                "Empresa", false, "America/Sao_Paulo", CorpoDeEmpresa.cnpjNovo(), "Empresa Ltda")))
                .isInstanceOfSatisfying(ConflitoException.class, excecao -> assertThat(excecao.getCodigo())
                        .isEqualTo("EMPRESA_COM_CONTA_NATY"));
        verify(sessaoService, never()).revogarTodasDaEmpresa(any());
    }

    @Test
    void exclusaoQuePerdeACorridaParaUmCadastroViraConflito() {
        Empresa empresa = empresaAtiva();
        doThrow(new DataIntegrityViolationException("fk"))
                .when(empresaRepository)
                .flush();

        assertThatThrownBy(() -> service.excluir(empresa.getId()))
                .isInstanceOfSatisfying(ConflitoException.class, excecao -> assertThat(excecao.getCodigo())
                        .isEqualTo("EMPRESA_COM_VINCULOS"));
    }
}
