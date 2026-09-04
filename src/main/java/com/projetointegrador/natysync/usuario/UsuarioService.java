package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.RecursoNaoEncontradoException;
import com.projetointegrador.natysync.usuario.dto.SessaoResponse;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public SessaoResponse resolverPorEmail(String email) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository
                .buscarPorEmailNormalizado(emailNormalizado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Integrante nao encontrado: " + emailNormalizado));
        return new SessaoResponse(usuario.getId(), usuario.getEmpresa().getId(), usuario.getNome(), usuario.getEmail());
    }
}
