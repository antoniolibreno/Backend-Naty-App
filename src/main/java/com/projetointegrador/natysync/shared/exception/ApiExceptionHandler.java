package com.projetointegrador.natysync.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException excecao, HttpServletRequest requisicao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResposta(
                        OffsetDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "RECURSO_NAO_ENCONTRADO",
                        excecao.getMessage(),
                        requisicao.getRequestURI(),
                        List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarFalhaDeValidacao(
            MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
        List<ErroResposta.ErroCampo> campos = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResposta.ErroCampo(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResposta(
                        OffsetDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "FALHA_DE_VALIDACAO",
                        "Requisicao invalida",
                        requisicao.getRequestURI(),
                        campos));
    }
}
