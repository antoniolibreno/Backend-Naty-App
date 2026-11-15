package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Atividade com vídeo e indicação de quiz")
public record AtividadeDetalheResponse(
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

        @Schema(
                description = "URL do vídeo no YouTube. Nulo sem vídeo",
                example = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        String videoUrl,

        @Schema(description = "Duração do vídeo em segundos", example = "180")
        Integer duracaoSegundos,

        @Schema(description = "Pontos da primeira conclusão", example = "50")
        Integer xp,

        @Schema(description = "Indica se a atividade tem quiz", example = "true")
        boolean possuiQuiz) {}
