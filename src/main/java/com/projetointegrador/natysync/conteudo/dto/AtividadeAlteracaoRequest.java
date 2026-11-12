package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Novos dados da atividade")
public record AtividadeAlteracaoRequest(
        @Schema(
                description = "Título, até 255 caracteres",
                example = "Boas-vindas ao treinamento",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String titulo,

        @Schema(description = "Descrição", example = "Conceitos iniciais e primeiros passos")
        String descricao,

        @Schema(
                description = "URL da imagem",
                example = "https://cdn.acme.com.br/atividades/boas-vindas.png",
                maxLength = 500)
        @Size(max = 500) String imagemUrl,

        @Schema(
                description = "URL HTTPS do YouTube. Exige duração",
                example = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                maxLength = 500)
        @Size(max = 500) String videoUrl,

        @Schema(description = "Duração em segundos. Só com vídeo", example = "212")
        Integer duracaoSegundos,

        @Schema(description = "XP da primeira conclusão. Padrão 10", example = "10", minimum = "0") @PositiveOrZero Integer xp) {

    public AtividadeAlteracaoRequest {
        if (titulo != null) {
            titulo = titulo.trim();
        }
        imagemUrl = RegraDeVideo.ausenteSeVazio(imagemUrl);
        videoUrl = RegraDeVideo.ausenteSeVazio(videoUrl);
        xp = RegraDeVideo.xpOuPadrao(xp);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "video deve ser URL HTTPS do YouTube") public boolean isVideoValido() {
        return RegraDeVideo.urlAceita(videoUrl);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "duracao deve ser positiva e existir so com video") public boolean isDuracaoCoerente() {
        return RegraDeVideo.duracaoCoerente(videoUrl, duracaoSegundos);
    }
}
