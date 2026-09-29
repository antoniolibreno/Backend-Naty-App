package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.CredencialInvalidaException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenSessaoFiltro extends OncePerRequestFilter {

    public static final String CABECALHO_AUTORIZACAO = "Authorization";
    public static final String PREFIXO_BEARER = "Bearer ";

    private final SessaoService sessaoService;

    public TokenSessaoFiltro(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest requisicao, @NonNull HttpServletResponse resposta, @NonNull FilterChain cadeia)
            throws ServletException, IOException {
        String token = lerToken(requisicao);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(token, requisicao);
        }
        cadeia.doFilter(requisicao, resposta);
    }

    private void autenticar(String token, HttpServletRequest requisicao) {
        try {
            IntegranteDaRequisicao integrante = sessaoService.resolverPorToken(token);
            UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                    integrante,
                    null,
                    List.of(new SimpleGrantedAuthority(
                            "ROLE_" + integrante.papel().name())));
            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        } catch (CredencialInvalidaException excecao) {
            SecurityContextHolder.clearContext();
        }
    }

    private String lerToken(HttpServletRequest requisicao) {
        String cabecalho = requisicao.getHeader(CABECALHO_AUTORIZACAO);
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO_BEARER)) {
            return null;
        }
        String token = cabecalho.substring(PREFIXO_BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
