package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.CredencialInvalidaException;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private static final String RECUSA_UNICA = "E-mail ou senha invalidos.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorDeSenha;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder codificadorDeSenha) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    public Usuario verificarCredencial(String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository
                .buscarPorEmailNormalizado(emailNormalizado)
                .orElseThrow(() -> new CredencialInvalidaException(RECUSA_UNICA));
        if (!usuario.podeEntrar() || !senhaConfere(senha, usuario.getSenhaHash())) {
            throw new CredencialInvalidaException(RECUSA_UNICA);
        }
        return usuario;
    }

    private boolean senhaConfere(String senha, String hashGuardado) {
        if (hashGuardado == null) {
            return false;
        }
        return codificadorDeSenha.matches(senha, hashGuardado);
    }
}
