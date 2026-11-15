package com.projetointegrador.natysync.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequisicaoIdFiltro extends OncePerRequestFilter {

    public static final String CABECALHO = "X-Request-Id";
    public static final String CHAVE_NO_MDC = "requisicaoId";
    private static final Pattern FORMATO_ACEITO = Pattern.compile("^[A-Za-z0-9-]{8,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String identificador = identificadorDa(requisicao);
        MDC.put(CHAVE_NO_MDC, identificador);
        resposta.setHeader(CABECALHO, identificador);
        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            MDC.remove(CHAVE_NO_MDC);
        }
    }

    private String identificadorDa(HttpServletRequest requisicao) {
        String enviado = requisicao.getHeader(CABECALHO);
        if (enviado != null && FORMATO_ACEITO.matcher(enviado).matches()) {
            return enviado;
        }
        return UUID.randomUUID().toString();
    }
}
