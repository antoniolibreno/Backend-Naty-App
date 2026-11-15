package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.shared.exception.CredencialInvalidaException;
import com.projetointegrador.natysync.shared.pagina.PaginaResponse;
import com.projetointegrador.natysync.usuario.dto.UsuarioResponse;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private static final String RECUSA_UNICA = "E-mail ou senha invalidos.";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder codificadorDeSenha;
    private final String hashFicticio;

    public UsuarioService(
            UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper, PasswordEncoder codificadorDeSenha) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.codificadorDeSenha = codificadorDeSenha;
        this.hashFicticio = codificadorDeSenha.encode(UUID.randomUUID().toString());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Usuario verificarCredencial(String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> comCredencial = usuarioRepository
                .travarPorEmailNormalizado(emailNormalizado)
                .filter(usuario -> usuario.getSenhaHash() != null);
        String hashComparado = comCredencial.map(Usuario::getSenhaHash).orElse(hashFicticio);
        boolean senhaConfere = codificadorDeSenha.matches(senha, hashComparado);
        return comCredencial
                .filter(usuario -> senhaConfere && usuario.podeEntrar())
                .orElseThrow(() -> new CredencialInvalidaException(RECUSA_UNICA));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioResponse> listarAtivosDaEmpresa(UUID empresaId, Pageable pagina) {
        PageRequest pedido = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), Sort.by("nome", "id"));
        return PaginaResponse.de(
                usuarioRepository.findByEmpresaIdAndAtivoTrue(empresaId, pedido), usuarioMapper::paraResposta);
    }
}
