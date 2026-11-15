package com.projetointegrador.natysync.progresso;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.IntegracaoTest;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ContagemDeConclusaoTest extends IntegracaoTest {

    private static final UUID PRIMEIRO_MODULO = UUID.fromString("00000000-0000-0000-0001-000000000001");

    @Autowired
    private ProgressoAtividadeRepository progressoAtividadeRepository;

    @Autowired
    private TentativaQuizRepository tentativaQuizRepository;

    private UUID integranteId;
    private UUID trilhaVaziaId;
    private UUID moduloVazioId;

    @BeforeEach
    void preparar() {
        integranteId = criarIntegrante(
                criarEmpresa("Empresa contagem"), "Integrante Contagem", UUID.randomUUID() + "@contagem.teste");
        trilhaVaziaId = UUID.randomUUID();
        moduloVazioId = UUID.randomUUID();
        jdbc.update("insert into trilha (id, titulo, ordem) values (?, 'Trilha vazia', 96)", trilhaVaziaId);
        jdbc.update(
                "insert into modulo (id, trilha_id, titulo, ordem) values (?, ?, 'Modulo vazio', 1)",
                moduloVazioId,
                trilhaVaziaId);
    }

    @AfterEach
    void limparConteudo() {
        jdbc.update("delete from modulo where id = ?", moduloVazioId);
        jdbc.update("delete from trilha where id = ?", trilhaVaziaId);
    }

    @Test
    void moduloETrilhaSemAtividadeNaoContamComoConcluidos() {
        assertThat(progressoAtividadeRepository.contarAtividadesConcluidas(integranteId))
                .isZero();
        assertThat(progressoAtividadeRepository.contarModulosConcluidos(integranteId))
                .isZero();
        assertThat(progressoAtividadeRepository.contarTrilhasConcluidas(integranteId))
                .isZero();
        assertThat(tentativaQuizRepository.maiorNotaDoIntegrante(integranteId)).isZero();
    }

    @Test
    void moduloContaQuandoTodaAtividadeDeleEstaConcluida() {
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, concluido_em)"
                        + " select ?, id, now() from atividade where modulo_id = ?",
                integranteId,
                PRIMEIRO_MODULO);

        assertThat(progressoAtividadeRepository.contarAtividadesConcluidas(integranteId))
                .isEqualTo(3);
        assertThat(progressoAtividadeRepository.contarModulosConcluidos(integranteId))
                .isEqualTo(1);
        assertThat(progressoAtividadeRepository.contarTrilhasConcluidas(integranteId))
                .isZero();
    }
}
