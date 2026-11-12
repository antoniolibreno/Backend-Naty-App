package com.projetointegrador.natysync.trilha.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Atividade dentro de um modulo, sem o conteudo de video")
public record AtividadeResumoResponse(
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
        Integer xp) {}
