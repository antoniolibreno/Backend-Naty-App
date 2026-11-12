package com.projetointegrador.natysync.progresso.dto;

import com.projetointegrador.natysync.progresso.EstadoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Atividade com o estado dela para o integrante")
public record AtividadeProgressoResponse(
        @Schema(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID id,

        @Schema(description = "Título da atividade", example = "Como cumprimentar o cliente")
        String titulo,

        @Schema(description = "Descrição da atividade", example = "Boas práticas para abrir uma conversa")
        String descricao,

        @Schema(description = "Posição no módulo, a partir de 1", example = "1")
        Integer ordem,

        @Schema(
                description = "URL da imagem de capa",
                example = "https://cdn.exemplo.com.br/atividades/cumprimento.png")
        String imagemUrl,

        @Schema(description = "Duração do vídeo em segundos", example = "180")
        Integer duracaoSegundos,

        @Schema(description = "Pontos da primeira conclusão", example = "50")
        Integer xp,

        @Schema(description = "Indica se a atividade tem quiz", example = "true")
        boolean possuiQuiz,

        @Schema(description = "Estado para o integrante", example = "DISPONIVEL")
        EstadoAtividade estado,

        @Schema(
                description = "Instante do vídeo assistido. Nulo se não houve",
                example = "2026-11-11T14:32:10.123-03:00",
                nullable = true)
        OffsetDateTime videoAssistidoEm,

        @Schema(
                description = "Instante da conclusão. Nulo se não concluída",
                example = "2026-11-11T14:35:42.456-03:00",
                nullable = true)
        OffsetDateTime concluidoEm) {}
