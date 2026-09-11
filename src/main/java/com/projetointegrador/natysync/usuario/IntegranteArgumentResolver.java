package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.IntegranteNaoInformadoException;
import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class IntegranteArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String CABECALHO_INTEGRANTE = "X-Integrante-Id";

    private final UsuarioRepository usuarioRepository;

    public IntegranteArgumentResolver(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return IntegranteDaRequisicao.class.equals(parametro.getParameterType());
    }

    @Override
    public IntegranteDaRequisicao resolveArgument(
            MethodParameter parametro,
            ModelAndViewContainer container,
            NativeWebRequest requisicao,
            WebDataBinderFactory fabricaDeBinder) {
        UUID usuarioId = lerIdentificador(requisicao.getHeader(CABECALHO_INTEGRANTE));
        Usuario usuario = usuarioRepository
                .buscarComEmpresaPorId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Integrante nao encontrado: " + usuarioId));
        return new IntegranteDaRequisicao(usuario.getId(), usuario.getEmpresa().getId());
    }

    private UUID lerIdentificador(String cabecalho) {
        if (cabecalho == null || cabecalho.isBlank()) {
            throw new IntegranteNaoInformadoException(
                    "Cabecalho " + CABECALHO_INTEGRANTE + " ausente. Informe o identificador do integrante.");
        }
        try {
            return UUID.fromString(cabecalho.trim());
        } catch (IllegalArgumentException excecao) {
            throw new IntegranteNaoInformadoException(
                    "Cabecalho " + CABECALHO_INTEGRANTE + " nao e um identificador valido.");
        }
    }
}
