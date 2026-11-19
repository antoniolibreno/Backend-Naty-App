package com.projetointegrador.natysync.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequisicaoIdFiltroTest {

    private final RequisicaoIdFiltro filtro = new RequisicaoIdFiltro();

    private String executar(String enviado, List<String> vistoNoMdc) throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        if (enviado != null) {
            requisicao.addHeader(RequisicaoIdFiltro.CABECALHO, enviado);
        }
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        filtro.doFilter(requisicao, resposta, (req, res) -> vistoNoMdc.add(MDC.get(RequisicaoIdFiltro.CHAVE_NO_MDC)));
        return resposta.getHeader(RequisicaoIdFiltro.CABECALHO);
    }

    @Test
    void geraIdentificadorQuandoNaoEnviado() throws Exception {
        List<String> noMdc = new ArrayList<>();

        String devolvido = executar(null, noMdc);

        assertThat(devolvido).isNotBlank();
        assertThat(noMdc).containsExactly(devolvido);
    }

    @Test
    void reaproveitaIdentificadorValidoDoCliente() throws Exception {
        List<String> noMdc = new ArrayList<>();

        String devolvido = executar("abc12345-def", noMdc);

        assertThat(devolvido).isEqualTo("abc12345-def");
        assertThat(noMdc).containsExactly("abc12345-def");
    }

    @Test
    void descartaValorForaDoFormato() throws Exception {
        for (String invalido : List.of("curto", "com espaco no meio", "x".repeat(65), "abc12345\nlinha")) {
            String devolvido = executar(invalido, new ArrayList<>());

            assertThat(devolvido).isNotEqualTo(invalido);
            assertThat(devolvido).matches("^[A-Za-z0-9-]{8,64}$");
        }
    }

    @Test
    void limpaOMdcAoFinal() throws Exception {
        executar("abc12345-def", new ArrayList<>());

        assertThat(MDC.get(RequisicaoIdFiltro.CHAVE_NO_MDC)).isNull();
    }
}
