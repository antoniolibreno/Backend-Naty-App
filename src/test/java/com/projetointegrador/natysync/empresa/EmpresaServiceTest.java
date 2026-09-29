package com.projetointegrador.natysync.empresa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.empresa.dto.EmpresaRequest;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class EmpresaServiceTest {
    private final EmpresaRepository repository = mock(EmpresaRepository.class);
    private final EmpresaService service = new EmpresaService(repository, Mappers.getMapper(EmpresaMapper.class));

    @Test
    void criarNormalizaONome() {
        when(repository.save(any(Empresa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = service.criar(new EmpresaRequest(" Empresa Teste ", true));

        assertThat(resposta.nome()).isEqualTo("Empresa Teste");
        assertThat(resposta.ativa()).isTrue();
    }

    @Test
    void atualizarPreservaIdentificador() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        when(repository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        when(repository.save(any(Empresa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = service.atualizar(empresa.getId(), new EmpresaRequest("Novo nome", false));

        assertThat(resposta.id()).isEqualTo(empresa.getId());
        assertThat(resposta.nome()).isEqualTo("Novo nome");
        assertThat(resposta.ativa()).isFalse();
    }

    @Test
    void operacoesComEmpresaInexistenteRetornamErro() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(id)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.atualizar(id, new EmpresaRequest("Nome", true)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.excluir(id)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluirEmpresaVinculadaRetornaConflito() {
        Empresa empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        when(repository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
        doThrow(new DataIntegrityViolationException("FK usuario"))
                .when(repository)
                .flush();

        assertThatThrownBy(() -> service.excluir(empresa.getId()))
                .isInstanceOfSatisfying(ResponseStatusException.class, excecao -> assertThat(excecao.getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
