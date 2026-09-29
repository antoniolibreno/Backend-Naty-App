package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.empresa.EmpresaRepository;
import com.projetointegrador.natysync.painel.dto.SenhaRequest;
import com.projetointegrador.natysync.usuario.Papel;
import com.projetointegrador.natysync.usuario.Usuario;
import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class BootstrapNaty implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder codificadorDeSenha;
    private final boolean obrigatorio;
    private final String nome;
    private final String email;
    private final String senha;
    private final String nomeDaEmpresa;

    public BootstrapNaty(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder codificadorDeSenha,
            @Value("${app.bootstrap.obrigatorio}") boolean obrigatorio,
            @Value("${app.bootstrap.naty.nome}") String nome,
            @Value("${app.bootstrap.naty.email}") String email,
            @Value("${app.bootstrap.naty.senha}") String senha,
            @Value("${app.bootstrap.naty.empresa}") String nomeDaEmpresa) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.codificadorDeSenha = codificadorDeSenha;
        this.obrigatorio = obrigatorio;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.nomeDaEmpresa = nomeDaEmpresa;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        if (usuarioRepository.existsByPapel(Papel.NATY)) {
            return;
        }
        if (!variaveisPreenchidas()) {
            if (obrigatorio) {
                throw new IllegalStateException(
                        "Nenhuma conta NATY existe e as variaveis APP_BOOTSTRAP_NATY_* estao vazias.");
            }
            log.warn("Nenhuma conta NATY existe e o bootstrap nao foi configurado.");
            return;
        }
        if (senha.length() < SenhaRequest.TAMANHO_MINIMO || !new SenhaRequest(senha).isSenhaDentroDoLimite()) {
            throw new IllegalStateException("A senha do bootstrap precisa ter de 8 caracteres a 72 bytes.");
        }
        criarContaNaty();
    }

    private boolean variaveisPreenchidas() {
        return Stream.of(nome, email, senha, nomeDaEmpresa).allMatch(valor -> valor != null && !valor.isBlank());
    }

    private void criarContaNaty() {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existeEmailNormalizado(emailNormalizado)) {
            throw new IllegalStateException("O e-mail do bootstrap pertence a uma conta que nao e NATY.");
        }
        Empresa empresa = new Empresa();
        empresa.setNome(nomeDaEmpresa.trim());
        empresaRepository.save(empresa);

        Usuario usuario = new Usuario();
        usuario.setEmpresa(empresa);
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPapel(Papel.NATY);
        usuario.setPerfil("admin");
        usuario.setStatus("offline");
        usuario.setSenhaHash(codificadorDeSenha.encode(senha));
        usuarioRepository.save(usuario);
        log.info("Conta NATY criada pelo bootstrap na empresa {}.", empresa.getId());
    }
}
