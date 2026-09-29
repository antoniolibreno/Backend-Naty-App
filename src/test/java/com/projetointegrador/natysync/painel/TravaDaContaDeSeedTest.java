package com.projetointegrador.natysync.painel;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.projetointegrador.natysync.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class TravaDaContaDeSeedTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);

    private TravaDaContaDeSeed trava(String... perfis) {
        MockEnvironment ambiente = new MockEnvironment();
        ambiente.setActiveProfiles(perfis);
        return new TravaDaContaDeSeed(usuarioRepository, ambiente);
    }

    @Test
    void recusaSubirForaDeDevComContaDoSeed() {
        when(usuarioRepository.existsByIdIn(TravaDaContaDeSeed.CONTAS_DO_SEED)).thenReturn(true);

        assertThatThrownBy(() -> trava("prod").run(null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> trava().run(null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sobeForaDeDevSemContaDoSeed() {
        when(usuarioRepository.existsByIdIn(TravaDaContaDeSeed.CONTAS_DO_SEED)).thenReturn(false);

        assertThatCode(() -> trava("prod").run(null)).doesNotThrowAnyException();
    }

    @Test
    void emDevAContaDoSeedEhEsperada() {
        when(usuarioRepository.existsByIdIn(TravaDaContaDeSeed.CONTAS_DO_SEED)).thenReturn(true);

        assertThatCode(() -> trava("dev").run(null)).doesNotThrowAnyException();
    }
}
