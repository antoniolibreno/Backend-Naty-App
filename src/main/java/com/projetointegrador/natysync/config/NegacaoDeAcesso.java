package com.projetointegrador.natysync.config;

import com.projetointegrador.natysync.shared.exception.ErroResposta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class NegacaoDeAcesso implements AccessDeniedHandler {

    private final ObjectMapper serializador;

    public NegacaoDeAcesso(ObjectMapper serializador) {
        this.serializador = serializador;
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException excecao)
            throws IOException {
        resposta.setStatus(HttpStatus.FORBIDDEN.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        serializador.writeValue(
                resposta.getOutputStream(),
                new ErroResposta(
                        OffsetDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "ACESSO_NEGADO",
                        "Acesso negado.",
                        requisicao.getRequestURI(),
                        List.of()));
    }
}
