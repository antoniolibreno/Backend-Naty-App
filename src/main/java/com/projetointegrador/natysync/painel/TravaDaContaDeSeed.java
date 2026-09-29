package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.usuario.UsuarioRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class TravaDaContaDeSeed implements ApplicationRunner {

    static final String PERFIL_DE_DESENVOLVIMENTO = "dev";
    static final List<UUID> CONTAS_DO_SEED = List.of(
            UUID.fromString("00000000-0000-0000-1001-000000000001"),
            UUID.fromString("00000000-0000-0000-1001-000000000002"),
            UUID.fromString("00000000-0000-0000-1001-000000000003"));

    private final UsuarioRepository usuarioRepository;
    private final Environment ambiente;

    public TravaDaContaDeSeed(UsuarioRepository usuarioRepository, Environment ambiente) {
        this.usuarioRepository = usuarioRepository;
        this.ambiente = ambiente;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (ambiente.acceptsProfiles(Profiles.of(PERFIL_DE_DESENVOLVIMENTO))) {
            return;
        }
        if (usuarioRepository.existsByIdIn(CONTAS_DO_SEED)) {
            throw new IllegalStateException(
                    "Conta do seed de desenvolvimento encontrada fora do perfil dev. A senha dela e publica.");
        }
    }
}
