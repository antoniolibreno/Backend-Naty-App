package com.projetointegrador.natysync.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import com.projetointegrador.natysync.empresa.Empresa;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LimpezaDeSessaoTest extends IntegracaoTest {

    private static final int DIAS_DE_RETENCAO = 30;

    @Autowired
    private LimpezaDeSessao limpeza;

    private UUID usuarioId;
    private final OffsetDateTime agora = OffsetDateTime.now();

    @BeforeEach
    void criarIntegrante() {
        Empresa empresa = criarEmpresa("Empresa Limpeza");
        usuarioId = criarIntegrante(empresa, "Integrante Limpeza", emailUnico("limpeza"));
    }

    private UUID sessao(OffsetDateTime expiraEm, OffsetDateTime revogadoEm) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                "insert into sessao (id, usuario_id, token_hash, criado_em, expira_em, revogado_em)"
                        + " values (?, ?, ?, ?, ?, ?)",
                id,
                usuarioId,
                UUID.randomUUID().toString(),
                agora.minusDays(90),
                expiraEm,
                revogadoEm);
        return id;
    }

    private boolean existe(UUID sessaoId) {
        Integer total = jdbc.queryForObject("select count(*) from sessao where id = ?", Integer.class, sessaoId);
        return total != null && total > 0;
    }

    @Test
    void removeSessaoExpiradaAlemDoPrazo() {
        UUID antiga = sessao(agora.minusDays(DIAS_DE_RETENCAO + 1), null);

        limpeza.removerEncerradasAntesDe(agora);

        assertThat(existe(antiga)).isFalse();
    }

    @Test
    void removeSessaoRevogadaAlemDoPrazo() {
        UUID revogada = sessao(agora.plusDays(5), agora.minusDays(DIAS_DE_RETENCAO + 1));

        limpeza.removerEncerradasAntesDe(agora);

        assertThat(existe(revogada)).isFalse();
    }

    @Test
    void mantemSessaoExpiradaDentroDoPrazo() {
        UUID recente = sessao(agora.minusDays(DIAS_DE_RETENCAO - 1), null);

        limpeza.removerEncerradasAntesDe(agora);

        assertThat(existe(recente)).isTrue();
    }

    @Test
    void mantemSessaoRevogadaDentroDoPrazo() {
        UUID recente = sessao(agora.plusDays(5), agora.minusDays(DIAS_DE_RETENCAO - 1));

        limpeza.removerEncerradasAntesDe(agora);

        assertThat(existe(recente)).isTrue();
    }

    @Test
    void mantemSessaoValida() {
        UUID valida = sessao(agora.plusDays(5), null);

        limpeza.removerEncerradasAntesDe(agora);

        assertThat(existe(valida)).isTrue();
    }

    @Test
    void removerSessaoNaoApagaOIntegrante() {
        sessao(agora.minusDays(DIAS_DE_RETENCAO + 1), null);

        limpeza.removerEncerradasAntesDe(agora);

        assertThat(usuarioRepository.existsById(usuarioId)).isTrue();
    }
}
