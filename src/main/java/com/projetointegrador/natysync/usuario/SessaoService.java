package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.CredencialInvalidaException;
import com.projetointegrador.natysync.usuario.dto.SessaoRequest;
import com.projetointegrador.natysync.usuario.dto.SessaoResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessaoService {

    private static final int BYTES_DO_TOKEN = 32;

    private final UsuarioService usuarioService;
    private final SessaoRepository sessaoRepository;
    private final Duration expiracao;
    private final SecureRandom sorteador = new SecureRandom();

    public SessaoService(
            UsuarioService usuarioService,
            SessaoRepository sessaoRepository,
            @Value("${app.sessao.expiracao}") Duration expiracao) {
        this.usuarioService = usuarioService;
        this.sessaoRepository = sessaoRepository;
        this.expiracao = expiracao;
    }

    @Transactional
    public SessaoResponse autenticar(SessaoRequest requisicao) {
        Usuario usuario = usuarioService.verificarCredencial(requisicao.email(), requisicao.senha());
        String token = sortearToken();
        OffsetDateTime agora = agora();

        Sessao sessao = new Sessao();
        sessao.setUsuario(usuario);
        sessao.setTokenHash(hashDe(token));
        sessao.setCriadoEm(agora);
        sessao.setExpiraEm(agora.plus(expiracao));
        sessao.setUltimoAcessoEm(agora);
        sessaoRepository.save(sessao);

        return new SessaoResponse(
                usuario.getId(),
                usuario.getEmpresa().getId(),
                usuario.getNome(),
                usuario.getEmail(),
                token,
                sessao.getExpiraEm());
    }

    @Transactional
    public IntegranteDaRequisicao resolverPorToken(String token) {
        OffsetDateTime agora = agora();
        Sessao sessao = sessaoRepository
                .buscarComIntegrantePorTokenHash(hashDe(token))
                .filter(encontrada -> encontrada.estaValidaEm(agora))
                .filter(encontrada -> encontrada.getUsuario().podeEntrar())
                .orElseThrow(() -> new CredencialInvalidaException("Sessao invalida."));
        sessaoRepository.registrarAcesso(sessao.getId(), agora);
        Usuario usuario = sessao.getUsuario();
        return new IntegranteDaRequisicao(usuario.getId(), usuario.getEmpresa().getId(), usuario.getPapel());
    }

    @Transactional
    public void revogar(String token) {
        sessaoRepository.revogarPorTokenHash(hashDe(token), agora());
    }

    @Transactional
    public void revogarTodasDoIntegrante(UUID usuarioId) {
        sessaoRepository.revogarTodasDoIntegrante(usuarioId, agora());
    }

    @Transactional
    public void revogarTodasDaEmpresa(UUID empresaId) {
        sessaoRepository.revogarTodasDaEmpresa(empresaId, agora());
    }

    private String sortearToken() {
        byte[] bytes = new byte[BYTES_DO_TOKEN];
        sorteador.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashDe(String token) {
        try {
            MessageDigest digestor = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digestor.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException excecao) {
            throw new IllegalStateException("SHA-256 indisponivel na plataforma", excecao);
        }
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
    }
}
