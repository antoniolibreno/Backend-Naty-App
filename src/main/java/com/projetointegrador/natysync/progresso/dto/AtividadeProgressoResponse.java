package com.projetointegrador.natysync.progresso.dto;

import com.projetointegrador.natysync.progresso.EstadoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Atividade com o estado dela para o integrante")
public record AtividadeProgressoResponse(
        @Schema(description = "Identificador da atividade", example = "e2b96c04-5a18-4d7f-b3c2-8d0a4f1e9b55")
        UUID id,

        @Schema(description = "Titulo da atividade", example = "Como cumprimentar o cliente")
        String titulo,

        @Schema(description = "Descricao da atividade", example = "Boas praticas para abrir uma conversa")
        String descricao,

        @Schema(description = "Posicao da atividade no modulo, a partir de 1", example = "1")
        Integer ordem,

        @Schema(
                description = "URL da imagem de capa da atividade",
                example = "https://cdn.exemplo.com.br/atividades/cumprimento.png")
        String imagemUrl,

        @Schema(description = "Duracao do video em segundos. Nulo quando a atividade nao tem video", example = "180")
        Integer duracaoSegundos,

        @Schema(description = "Pontos concedidos na primeira conclusao da atividade", example = "50")
        Integer xp,

        @Schema(
                description = "Indica se a atividade tem quiz. Com quiz, a conclusao exige tentativa aprovada",
                example = "true")
        boolean possuiQuiz,

        @Schema(
                description = "Estado para o integrante. BLOQUEADO aguarda a conclusao da atividade anterior,"
                        + " DISPONIVEL pode ser feita e CONCLUIDO ja foi concluida",
                example = "DISPONIVEL")
        EstadoAtividade estado,

        @Schema(
                description = "Instante em que o video foi marcado como assistido. Nulo se nao foi",
                example = "2026-11-11T14:32:10.123-03:00",
                nullable = true)
        OffsetDateTime videoAssistidoEm,

        @Schema(
                description = "Instante da primeira conclusao da atividade. Nulo se nao concluida",
                example = "2026-11-11T14:35:42.456-03:00",
                nullable = true)
        OffsetDateTime concluidoEm) {}
