package com.projetointegrador.natysync.gamificacao;

import static org.assertj.core.api.Assertions.assertThat;

import com.projetointegrador.natysync.empresa.Empresa;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConquistaApiTest extends GamificacaoTest {

    private Empresa empresa;
    private Conta conta;

    @BeforeEach
    void preparar() {
        empresa = criarEmpresa("Empresa conquista");
        conta = novaConta(empresa, "Integrante Conquista");
    }

    @Test
    void primeiraAtividadeEhConcedidaUmaVezEMantemOInstanteOriginal() {
        aprovar(conta, ATIVIDADES_DA_TRILHA.get(0));
        String obtidaEm = conquista(conta, "PRIMEIRA_ATIVIDADE").get("obtidaEm").asText();

        aprovar(conta, ATIVIDADES_DA_TRILHA.get(1));

        assertThat(conquista(conta, "PRIMEIRA_ATIVIDADE").get("obtidaEm").asText())
                .isEqualTo(obtidaEm);
        assertThat(jdbc.queryForObject(
                        "select count(*) from conquista_obtida o join conquista c on c.id = o.conquista_id"
                                + " where o.usuario_id = ? and c.codigo = 'PRIMEIRA_ATIVIDADE'",
                        Integer.class,
                        conta.id()))
                .isEqualTo(1);
        assertThat(obtidaEm)
                .isEqualTo(
                        perfil(conta).get("conquistas").get(0).get("obtidaEm").asText());
    }

    @Test
    void quizPerfeitoExigeNotaCem() {
        reprovar(conta, ATIVIDADES_DA_TRILHA.getFirst());
        assertThat(obteve(conta, "QUIZ_PERFEITO")).isFalse();

        aprovar(conta, ATIVIDADES_DA_TRILHA.getFirst());

        assertThat(obteve(conta, "QUIZ_PERFEITO")).isTrue();
    }

    @Test
    void percorrerATrilhaConcedeModuloPontosETrilhaNoLimiar() {
        concluirAte(conta, 2);
        assertThat(obteve(conta, "MODULO_CONCLUIDO")).isFalse();

        concluirAte(conta, 4);
        assertThat(obteve(conta, "MODULO_CONCLUIDO")).isTrue();
        assertThat(pontos(conta)).isEqualTo(40);
        assertThat(obteve(conta, "PONTOS_50")).isFalse();
        assertThat(obteve(conta, "TRILHA_CONCLUIDA")).isFalse();

        concluirAte(conta, 5);
        assertThat(obteve(conta, "PONTOS_50")).isTrue();
        assertThat(obteve(conta, "TRILHA_CONCLUIDA")).isFalse();

        concluirAte(conta, 6);
        assertThat(obteve(conta, "TRILHA_CONCLUIDA")).isTrue();
    }

    @Test
    void sequenciaDeTresDiasEhConcedidaEPreservadaDepoisDaQuebra() {
        LocalDate diaCorrente = LocalDate.now(empresa.getFusoHorario());
        registrarDiaDeEstudo(diaCorrente.minusDays(1));
        registrarDiaDeEstudo(diaCorrente.minusDays(2));

        reprovar(conta, ATIVIDADES_DA_TRILHA.getFirst());

        assertThat(obteve(conta, "SEQUENCIA_3_DIAS")).isTrue();
        assertThat(obteve(conta, "SEQUENCIA_7_DIAS")).isFalse();
        assertThat(perfil(conta).get("sequencia").get("atual").asInt()).isEqualTo(3);

        jdbc.update("update dia_de_estudo set dia = dia - 3 where usuario_id = ?", conta.id());

        assertThat(perfil(conta).get("sequencia").get("atual").asInt()).isZero();
        assertThat(perfil(conta).get("sequencia").get("maior").asInt()).isEqualTo(3);
        assertThat(obteve(conta, "SEQUENCIA_3_DIAS")).isTrue();
    }

    @Test
    void progressoGravadoSemAvaliacaoRecebeConquistaNoEstudoSeguinte() {
        jdbc.update(
                "insert into progresso_atividade (usuario_id, atividade_id, concluido_em) values (?, ?, now())",
                conta.id(),
                ATIVIDADES_DA_TRILHA.getFirst());
        assertThat(obteve(conta, "PRIMEIRA_ATIVIDADE")).isFalse();

        reprovar(conta, ATIVIDADES_DA_TRILHA.get(1));

        assertThat(obteve(conta, "PRIMEIRA_ATIVIDADE")).isTrue();
    }

    private void registrarDiaDeEstudo(LocalDate dia) {
        jdbc.update("insert into dia_de_estudo (usuario_id, dia) values (?, ?)", conta.id(), dia);
    }
}
