package com.projetointegrador.natysync.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException excecao, HttpServletRequest requisicao) {
        return erro(HttpStatus.NOT_FOUND, "RECURSO_NAO_ENCONTRADO", excecao.getMessage(), requisicao);
    }

    @ExceptionHandler(CredencialInvalidaException.class)
    public ResponseEntity<ErroResposta> tratarCredencialInvalida(
            CredencialInvalidaException excecao, HttpServletRequest requisicao) {
        return erro(HttpStatus.UNAUTHORIZED, "CREDENCIAL_INVALIDA", excecao.getMessage(), requisicao);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResposta> tratarAcessoNegado(
            AcessoNegadoException excecao, HttpServletRequest requisicao) {
        return erro(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", excecao.getMessage(), requisicao);
    }

    @ExceptionHandler(AtividadeBloqueadaException.class)
    public ResponseEntity<ErroResposta> tratarAtividadeBloqueada(
            AtividadeBloqueadaException excecao, HttpServletRequest requisicao) {
        return erro(HttpStatus.CONFLICT, "ATIVIDADE_BLOQUEADA", excecao.getMessage(), requisicao);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> tratarConflito(ConflitoException excecao, HttpServletRequest requisicao) {
        return erro(HttpStatus.CONFLICT, excecao.getCodigo(), excecao.getMessage(), requisicao);
    }

    @ExceptionHandler(TentativaQuizInvalidaException.class)
    public ResponseEntity<ErroResposta> tratarTentativaQuizInvalida(
            TentativaQuizInvalidaException excecao, HttpServletRequest requisicao) {
        return ResponseEntity.badRequest().body(corpo(
                HttpStatus.BAD_REQUEST,
                "TENTATIVA_QUIZ_INVALIDA",
                excecao.getMessage(),
                requisicao.getRequestURI(),
                List.of(new ErroResposta.ErroCampo(excecao.getCampo(), excecao.getMessage()))));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excecao,
            HttpHeaders cabecalhos,
            HttpStatusCode status,
            WebRequest requisicao) {
        List<ErroResposta.ErroCampo> campos = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResposta.ErroCampo(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(cabecalhos)
                .body(corpo(
                        HttpStatus.BAD_REQUEST,
                        "FALHA_DE_VALIDACAO",
                        "Requisicao invalida",
                        caminhoDe(requisicao),
                        campos));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException excecao,
            HttpHeaders cabecalhos,
            HttpStatusCode status,
            WebRequest requisicao) {
        List<ErroResposta.ErroCampo> campos = excecao.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new ErroResposta.ErroCampo(
                                resultado.getMethodParameter().getParameterName(), erro.getDefaultMessage())))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(cabecalhos)
                .body(corpo(
                        HttpStatus.BAD_REQUEST,
                        "FALHA_DE_VALIDACAO",
                        "Requisicao invalida",
                        caminhoDe(requisicao),
                        campos));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception excecao,
            Object corpoPadrao,
            HttpHeaders cabecalhos,
            HttpStatusCode status,
            WebRequest requisicao) {
        if (respostaJaEnviada(requisicao)) {
            return null;
        }
        if (status.is5xxServerError()) {
            logger.error("Erro do framework na requisicao " + caminhoDe(requisicao), excecao);
        }
        return ResponseEntity.status(status)
                .headers(cabecalhos)
                .body(corpo(
                        status, codigoDoStatus(status), mensagemDoStatus(status), caminhoDe(requisicao), List.of()));
    }

    private static boolean respostaJaEnviada(WebRequest requisicao) {
        return requisicao instanceof ServletWebRequest servlet
                && servlet.getResponse() != null
                && servlet.getResponse().isCommitted();
    }

    private static String codigoDoStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "REQUISICAO_MALFORMADA";
            case 401 -> "CREDENCIAL_INVALIDA";
            case 403 -> "ACESSO_NEGADO";
            case 404 -> "RECURSO_NAO_ENCONTRADO";
            case 405 -> "METODO_NAO_SUPORTADO";
            case 406 -> "FORMATO_NAO_ACEITO";
            case 415 -> "TIPO_DE_CONTEUDO_NAO_SUPORTADO";
            case 500 -> "ERRO_INTERNO";
            default -> {
                HttpStatus conhecido = HttpStatus.resolve(status.value());
                yield conhecido == null ? "ERRO_" + status.value() : conhecido.name();
            }
        };
    }

    private static String mensagemDoStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisicao malformada.";
            case 401 -> "Sessao invalida.";
            case 403 -> "Acesso negado.";
            case 404 -> "Recurso nao encontrado.";
            case 405 -> "Metodo nao suportado nesta rota.";
            case 406 -> "Formato de resposta nao aceito.";
            case 415 -> "Tipo de conteudo nao suportado.";
            case 500 -> "Erro interno.";
            default -> "Requisicao nao atendida.";
        };
    }

    private static ResponseEntity<ErroResposta> erro(
            HttpStatus status, String codigo, String mensagem, HttpServletRequest requisicao) {
        return ResponseEntity.status(status)
                .body(corpo(status, codigo, mensagem, requisicao.getRequestURI(), List.of()));
    }

    private static ErroResposta corpo(
            HttpStatusCode status,
            String codigo,
            String mensagem,
            String caminho,
            List<ErroResposta.ErroCampo> campos) {
        return new ErroResposta(OffsetDateTime.now(), status.value(), codigo, mensagem, caminho, campos);
    }

    private static String caminhoDe(WebRequest requisicao) {
        return requisicao instanceof ServletWebRequest servlet
                ? servlet.getRequest().getRequestURI()
                : null;
    }
}
