package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.CredencialInvalidaException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class IntegranteArgumentResolver implements HandlerMethodArgumentResolver {

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
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof IntegranteDaRequisicao integrante)) {
            throw new CredencialInvalidaException("Sessao invalida.");
        }
        return integrante;
    }
}
