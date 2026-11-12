package com.projetointegrador.natysync.conteudo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Alteracao dos dados da atividade. Substitui todos os campos do corpo")
public record AtividadeAlteracaoRequest(
        @Schema(
                description = "Titulo, sem espacos nas pontas, ate 255 caracteres",
                example = "Boas-vindas ao treinamento",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 255)
        @NotBlank @Size(max = 255) String titulo,

        @Schema(description = "Descricao livre da atividade", example = "Conceitos iniciais e primeiros passos")
        String descricao,

        @Schema(
                description = "URL da imagem da atividade. Em branco vale como sem imagem",
                example = "https://cdn.acme.com.br/atividades/boas-vindas.png",
                maxLength = 500)
        @Size(max = 500) String imagemUrl,

        @Schema(
                description =
                        "URL do video, HTTPS do YouTube. Omitido ou em branco vale como sem video. Exige duracaoSegundos positiva",
                example = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                maxLength = 500)
        @Size(max = 500) String videoUrl,

        @Schema(
                description =
                        "Duracao do video em segundos. Positiva com video e ausente sem video. O integrante so conclui apos esse tempo desde o inicio registrado",
                example = "212")
        Integer duracaoSegundos,

        @Schema(
                description = "XP concedido na primeira conclusao da atividade. Vale 10 quando omitido",
                example = "10",
                minimum = "0")
        @PositiveOrZero Integer xp) {

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
