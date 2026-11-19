package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DiaDeEstudoApiTest extends GamificacaoTest {

    private static final UUID PRIMEIRA = ATIVIDADES_DA_TRILHA.getFirst();
    private UUID trilhaId;

    @AfterEach
    void limparConteudo() {
        if (trilhaId != null) {
            jdbc.update(
                    "delete from progresso_atividade where atividade_id in"
                            + " (select a.id from atividade a join modulo m on m.id = a.modulo_id where m.trilha_id = ?)",
                    trilhaId);
            jdbc.update(
                    "delete from atividade where modulo_id in (select id from modulo where trilha_id = ?)", trilhaId);
            jdbc.update("delete from modulo where trilha_id = ?", trilhaId);
            jdbc.update("delete from trilha where id = ?", trilhaId);
        }
    }

    @Test
    void tentativaReprovadaMarcaODiaCorrenteDaEmpresa() {
        Empresa empresa = criarEmpresa("Empresa dia reprovado");
        Conta conta = novaConta(empresa, "Integrante Reprovado");

        reprovar(conta, PRIMEIRA);

        assertThat(diasDeEstudo(conta)).containsExactly(LocalDate.now(empresa.getFusoHorario()));
    }

    @Test
    void variosEstudosNoMesmoDiaContamUmaVez() {
        Conta conta = novaConta(criarEmpresa("Empresa dia repetido"), "Integrante Repetido");

        reprovar(conta, PRIMEIRA);
        aprovar(conta, PRIMEIRA);
        aprovar(conta, PRIMEIRA);

        assertThat(diasDeEstudo(conta)).hasSize(1);
    }

    @Test
    void inicioDeVideoNaoMarcaDia() {
        Conta conta = novaConta(criarEmpresa("Empresa dia video"), "Integrante Video");
        trilhaId = UUID.randomUUID();
        UUID moduloId = UUID.randomUUID();
        UUID atividadeId = UUID.randomUUID();
        jdbc.update("insert into trilha (id, titulo, ordem) values (?, 'Trilha Dia', 97)", trilhaId);
        jdbc.update(
                "insert into modulo (id, trilha_id, titulo, ordem) values (?, ?, 'Modulo Dia', 1)", moduloId, trilhaId);
        jdbc.update(
                "insert into atividade (id, modulo_id, titulo, ordem, video_url, duracao_segundos)"
                        + " values (?, ?, 'Video', 1, 'https://youtu.be/abcdefghijk', 120)",
                atividadeId,
                moduloId);

        cliente(conta.token())
                .post()
                .uri("/api/v1/atividades/{id}/video-iniciado", atividadeId)
                .retrieve()
                .toBodilessEntity();

        assertThat(diasDeEstudo(conta)).isEmpty();
    }

    @Test
    void diaGravadoEhADataLocalDoFusoDaEmpresa() {
        ZoneId fuso = fusoComDataDiferenteDaUtc();
        Empresa empresa = criarEmpresa("Empresa dia fuso");
        empresa.setFusoHorario(fuso);
        empresaRepository.save(empresa);
        Conta conta = novaConta(empresa, "Integrante Fuso");

        reprovar(conta, PRIMEIRA);

        assertThat(diasDeEstudo(conta)).containsExactly(LocalDate.now(fuso));
        assertThat(diasDeEstudo(conta)).doesNotContain(LocalDate.now(ZoneOffset.UTC));
    }

    private ZoneId fusoComDataDiferenteDaUtc() {
        ZoneId adiantado = ZoneId.of("Pacific/Kiritimati");
        if (!LocalDate.now(adiantado).equals(LocalDate.now(ZoneOffset.UTC))) {
            return adiantado;
        }
        return ZoneId.of("Pacific/Pago_Pago");
    }
}
