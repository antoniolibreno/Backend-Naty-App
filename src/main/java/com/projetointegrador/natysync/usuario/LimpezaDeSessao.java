package com.projetointegrador.natysync.usuario;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LimpezaDeSessao {

    private final SessaoRepository sessaoRepository;
    private final Duration retencao;

    public LimpezaDeSessao(SessaoRepository sessaoRepository, @Value("${app.sessao.retencao}") Duration retencao) {
        this.sessaoRepository = sessaoRepository;
        this.retencao = retencao;
    }

    @Scheduled(cron = "${app.sessao.limpeza.cron}")
    public void removerAntigas() {
        removerEncerradasAntesDe(OffsetDateTime.now());
    }

    @Transactional
    public int removerEncerradasAntesDe(OffsetDateTime agora) {
        return sessaoRepository.removerEncerradasAntesDe(agora.minus(retencao));
    }
}
