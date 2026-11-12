package com.projetointegrador.natysync.conteudo.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AtividadeAlteracaoRequest(
        @NotBlank @Size(max = 255) String titulo,
        String descricao,
        @Size(max = 500) String imagemUrl,
        @Size(max = 500) String videoUrl,
        Integer duracaoSegundos,
        @PositiveOrZero Integer xp) {

    public AtividadeAlteracaoRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
        imagemUrl = RegraDeVideo.ausenteSeVazio(imagemUrl);
        videoUrl = RegraDeVideo.ausenteSeVazio(videoUrl);
        xp = RegraDeVideo.xpOuPadrao(xp);
    }

    @AssertTrue(message = "video deve ser URL HTTPS do YouTube") public boolean isVideoValido() {
        return RegraDeVideo.urlAceita(videoUrl);
    }

    @AssertTrue(message = "duracao deve ser positiva e existir so com video") public boolean isDuracaoCoerente() {
        return RegraDeVideo.duracaoCoerente(videoUrl, duracaoSegundos);
    }
}
