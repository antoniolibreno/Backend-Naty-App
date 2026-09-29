package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.painel.dto.EmpresaRequest;
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
    private final EmpresaService service = new EmpresaService(
            empresaRepository, usuarioRepository, sessaoService, Mappers.getMapper(EmpresaMapper.class));

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

        var resposta = service.criar(new EmpresaRequest(" Empresa Teste ", true, "America/Manaus"));

        assertThat(resposta.nome()).isEqualTo("Empresa Teste");
        assertThat(resposta.fusoHorario()).isEqualTo("America/Manaus");
    }

    @Test
    void desativarRevogaAsSessoesDaEmpresa() {
        Empresa empresa = empresaAtiva();

        service.atualizar(empresa.getId(), new EmpresaRequest("Empresa", false, "America/Sao_Paulo"));

        verify(usuarioRepository).travarTodosDaEmpresa(empresa.getId());
        verify(sessaoService).revogarTodasDaEmpresa(empresa.getId());
        assertThat(empresa.getFusoHorario()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
    }

    @Test
    void alterarSemDesativarNaoRevogaSessao() {
        Empresa empresa = empresaAtiva();

        service.atualizar(empresa.getId(), new EmpresaRequest("Outro nome", true, "America/Sao_Paulo"));

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

        assertThatThrownBy(() ->
                        service.atualizar(empresa.getId(), new EmpresaRequest("Empresa", false, "America/Sao_Paulo")))
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
