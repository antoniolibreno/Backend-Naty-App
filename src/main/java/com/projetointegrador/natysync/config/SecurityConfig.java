package com.projetointegrador.natysync.config;

import com.projetointegrador.natysync.usuario.TokenSessaoFiltro;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final String BCRYPT = "bcrypt";

    private static final String[] ROTAS_PUBLICAS = {
        "/actuator/health", "/actuator/health/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    @Bean
    public PasswordEncoder codificadorDeSenha(@Value("${app.senha.forca-hash}") int forcaHash) {
        return new DelegatingPasswordEncoder(BCRYPT, Map.of(BCRYPT, new BCryptPasswordEncoder(forcaHash)));
    }

    @Bean
    public SecurityFilterChain cadeiaDeSeguranca(
            HttpSecurity http, TokenSessaoFiltro tokenSessaoFiltro, RecusaDeAcesso recusaDeAcesso) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas.requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/sessoes")
                        .permitAll()
                        .requestMatchers(ROTAS_PUBLICAS)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(erros -> erros.authenticationEntryPoint(recusaDeAcesso))
                .addFilterBefore(tokenSessaoFiltro, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
