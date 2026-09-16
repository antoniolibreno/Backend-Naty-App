package com.projetointegrador.natysync.config;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RecusaDeAcesso implements AuthenticationEntryPoint {

    private final ObjectMapper serializador;

    public RecusaDeAcesso(ObjectMapper serializador) {
        this.serializador = serializador;
    }

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException excecao)
            throws IOException {
        resposta.setStatus(HttpStatus.UNAUTHORIZED.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        serializador.writeValue(
                resposta.getOutputStream(),
                new ErroResposta(
                        OffsetDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "CREDENCIAL_INVALIDA",
                        "Sessao invalida.",
                        requisicao.getRequestURI(),
                        List.of()));
    }
}
