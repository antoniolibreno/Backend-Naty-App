package com.projetointegrador.natysync.acompanhamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.projetointegrador.natysync.acompanhamento.CalculadoraDeAcompanhamento.AtividadeAtiva;
import com.projetointegrador.natysync.acompanhamento.dto.IntegranteAcompanhadoResponse;
import com.projetointegrador.natysync.trilha.TrilhaRepository;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalculadoraDeAcompanhamentoTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final UUID TRILHA = UUID.randomUUID();
    private static final OffsetDateTime AGORA = OffsetDateTime.parse("2026-03-10T15:00:00Z");

    private final List<AtividadeAtiva> atividades = List.of(
            new AtividadeAtiva(UUID.randomUUID(), TRILHA, "A1"),
            new AtividadeAtiva(UUID.randomUUID(), TRILHA, "A2"),
            new AtividadeAtiva(UUID.randomUUID(), TRILHA, "A3"),
            new AtividadeAtiva(UUID.randomUUID(), TRILHA, "A4"));

    private CalculadoraDeAcompanhamento calculadora(int diasParado) {
        return new CalculadoraDeAcompanhamento(
                mock(IntegrantesAcompanhadosRepository.class),
                mock(ConclusoesRepository.class),
                mock(TrilhaRepository.class),
                diasParado);
    }

    private Usuario integrante(OffsetDateTime criadoEm) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Ana");
        usuario.setEmail("ana@teste.com.br");
        usuario.setPapel(Papel.INTEGRANTE);
        usuario.setCriadoEm(criadoEm);
        return usuario;
    }

    private List<Conclusao> conclusoes(Usuario usuario, int quantas, OffsetDateTime ultimaEm) {
        List<Conclusao> lista = new ArrayList<>();
        for (int i = 0; i < quantas; i++) {
            lista.add(new Conclusao(
                    usuario.getId(),
                    atividades.get(i).atividadeId(),
                    i == quantas - 1 ? ultimaEm : ultimaEm.minusDays(1)));
        }
        return lista;
    }

    private IntegranteAcompanhadoResponse calcular(int diasParado, Usuario usuario, List<Conclusao> conclusoes) {
        return calculadora(diasParado).calcularDe(usuario, atividades, conclusoes, SAO_PAULO, AGORA);
    }

    @Test
    void concluirHaMenosDiasQueOPrazoEstaAvancando() {
        Usuario usuario = integrante(AGORA.minusDays(30));

        IntegranteAcompanhadoResponse resposta = calcular(3, usuario, conclusoes(usuario, 2, AGORA.minusDays(2)));

        assertThat(resposta.situacao()).isEqualTo(SituacaoDeAcompanhamento.AVANCANDO);
        assertThat(resposta.diasSemConcluir()).isEqualTo(2);
        assertThat(resposta.atividadesConcluidas()).isEqualTo(2);
        assertThat(resposta.percentualConcluido()).isEqualTo(50);
        assertThat(resposta.atividadeAtual().titulo()).isEqualTo("A3");
    }

    @Test
    void oPrazoExatoJaEhParado() {
        Usuario usuario = integrante(AGORA.minusDays(30));

        IntegranteAcompanhadoResponse resposta = calcular(3, usuario, conclusoes(usuario, 1, AGORA.minusDays(3)));

        assertThat(resposta.situacao()).isEqualTo(SituacaoDeAcompanhamento.PARADO);
        assertThat(resposta.diasSemConcluir()).isEqualTo(3);
    }

    @Test
    void contaNovaSemEstudoEstaSemInicio() {
        IntegranteAcompanhadoResponse resposta = calcular(3, integrante(AGORA.minusDays(2)), List.of());

        assertThat(resposta.situacao()).isEqualTo(SituacaoDeAcompanhamento.SEM_INICIO);
        assertThat(resposta.diasSemConcluir()).isEqualTo(2);
        assertThat(resposta.ultimaConclusaoEm()).isNull();
        assertThat(resposta.atividadeAtual().titulo()).isEqualTo("A1");
    }

    @Test
    void contaAntigaSemEstudoEstaParada() {
        IntegranteAcompanhadoResponse resposta = calcular(3, integrante(AGORA.minusDays(3)), List.of());

        assertThat(resposta.situacao()).isEqualTo(SituacaoDeAcompanhamento.PARADO);
    }

    @Test
    void quemConcluiuTudoNuncaEstaParado() {
        Usuario usuario = integrante(AGORA.minusDays(90));

        IntegranteAcompanhadoResponse resposta = calcular(3, usuario, conclusoes(usuario, 4, AGORA.minusDays(40)));

        assertThat(resposta.situacao()).isEqualTo(SituacaoDeAcompanhamento.CONCLUIU);
        assertThat(resposta.percentualConcluido()).isEqualTo(100);
        assertThat(resposta.atividadeAtual()).isNull();
    }

    @Test
    void prazoMaiorTornaOMesmoIntegranteAvancando() {
        Usuario usuario = integrante(AGORA.minusDays(30));
        List<Conclusao> conclusoes = conclusoes(usuario, 1, AGORA.minusDays(5));

        assertThat(calcular(3, usuario, conclusoes).situacao()).isEqualTo(SituacaoDeAcompanhamento.PARADO);
        assertThat(calcular(7, usuario, conclusoes).situacao()).isEqualTo(SituacaoDeAcompanhamento.AVANCANDO);
    }

    @Test
    void osDiasSaoContadosNoFusoDaEmpresaENaoEmUtc() {
        Usuario usuario = integrante(AGORA.minusDays(30));
        OffsetDateTime concluidaEm = OffsetDateTime.parse("2026-03-07T04:00:00Z");
        OffsetDateTime agoraAindaNoDia9EmSaoPaulo = OffsetDateTime.parse("2026-03-10T02:00:00Z");
        List<Conclusao> conclusoes = conclusoes(usuario, 1, concluidaEm);

        IntegranteAcompanhadoResponse emSaoPaulo =
                calculadora(3).calcularDe(usuario, atividades, conclusoes, SAO_PAULO, agoraAindaNoDia9EmSaoPaulo);
        IntegranteAcompanhadoResponse emUtc =
                calculadora(3).calcularDe(usuario, atividades, conclusoes, ZoneOffset.UTC, agoraAindaNoDia9EmSaoPaulo);

        assertThat(emSaoPaulo.diasSemConcluir()).isEqualTo(2);
        assertThat(emSaoPaulo.situacao()).isEqualTo(SituacaoDeAcompanhamento.AVANCANDO);
        assertThat(emUtc.diasSemConcluir()).isEqualTo(3);
        assertThat(emUtc.situacao()).isEqualTo(SituacaoDeAcompanhamento.PARADO);
    }

    @Test
    void conclusaoDeAtividadeForaDasTrilhasAtivasNaoConta() {
        Usuario usuario = integrante(AGORA.minusDays(1));
        List<Conclusao> deTrilhaInativa =
                List.of(new Conclusao(usuario.getId(), UUID.randomUUID(), AGORA.minusDays(1)));

        IntegranteAcompanhadoResponse resposta = calcular(3, usuario, deTrilhaInativa);

        assertThat(resposta.atividadesConcluidas()).isZero();
        assertThat(resposta.ultimaConclusaoEm()).isNull();
        assertThat(resposta.situacao()).isEqualTo(SituacaoDeAcompanhamento.SEM_INICIO);
    }

    @Test
    void semAtividadeNasTrilhasAtivasNaoHaPercentualNemConclusao() {
        Usuario usuario = integrante(AGORA.minusDays(1));

        IntegranteAcompanhadoResponse resposta =
                calculadora(3).calcularDe(usuario, List.of(), List.of(), SAO_PAULO, AGORA);

        assertThat(resposta.totalAtividades()).isZero();
        assertThat(resposta.percentualConcluido()).isZero();
        assertThat(resposta.situacao()).isEqualTo(SituacaoDeAcompanhamento.SEM_INICIO);
    }
}
