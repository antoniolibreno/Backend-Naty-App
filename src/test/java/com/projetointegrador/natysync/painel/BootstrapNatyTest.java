package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

class BootstrapNatyTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final EmpresaRepository empresaRepository = mock(EmpresaRepository.class);

    private BootstrapNaty bootstrap(boolean obrigatorio, String email, String senha) {
        return new BootstrapNaty(
                usuarioRepository,
                empresaRepository,
                NoOpPasswordEncoder.getInstance(),
                mock(JdbcTemplate.class),
                obrigatorio,
                "Equipe Naty",
                email,
                senha,
                "Naty");
    }

    @Test
    void criaEmpresaInternaEContaNatyQuandoNaoExisteNenhuma() {
        when(usuarioRepository.existsByPapel(Papel.NATY)).thenReturn(false);

        bootstrap(true, " Naty@Naty.com ", "senha-forte-1").run(null);

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        verify(empresaRepository).save(any(Empresa.class));
        verify(usuarioRepository).save(usuario.capture());
        assertThat(usuario.getValue().getPapel()).isEqualTo(Papel.NATY);
        assertThat(usuario.getValue().getEmail()).isEqualTo("naty@naty.com");
        assertThat(usuario.getValue().getEmpresa().getNome()).isEqualTo("Naty");
    }

    @Test
    void naoCriaSegundaContaNaty() {
        when(usuarioRepository.existsByPapel(Papel.NATY)).thenReturn(true);

        bootstrap(true, "naty@naty.com", "senha-forte-1").run(null);

        verify(usuarioRepository, never()).save(any());
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void recusaSubirQuandoObrigatorioESemVariaveis() {
        when(usuarioRepository.existsByPapel(Papel.NATY)).thenReturn(false);

        assertThatThrownBy(() -> bootstrap(true, "", "").run(null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void segueSemContaQuandoNaoObrigatorioESemVariaveis() {
        when(usuarioRepository.existsByPapel(Papel.NATY)).thenReturn(false);

        bootstrap(false, "", "").run(null);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void recusaSenhaCurta() {
        when(usuarioRepository.existsByPapel(Papel.NATY)).thenReturn(false);

        assertThatThrownBy(() -> bootstrap(true, "naty@naty.com", "curta").run(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recusaSenhaAcimaDoLimiteDoBcrypt() {
        when(usuarioRepository.existsByPapel(Papel.NATY)).thenReturn(false);

        assertThatThrownBy(
                        () -> bootstrap(true, "naty@naty.com", "é".repeat(37)).run(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recusaEmailQuePertenceAContaQueNaoEhNaty() {
        when(usuarioRepository.existsByPapel(Papel.NATY)).thenReturn(false);
        when(usuarioRepository.existeEmailNormalizado("admin@cliente.com")).thenReturn(true);

        assertThatThrownBy(() ->
                        bootstrap(true, "admin@cliente.com", "senha-forte-1").run(null))
                .isInstanceOf(IllegalStateException.class);
        verify(usuarioRepository, never()).save(any());
    }
}
