package com.projetointegrador.natysync.shared.pagina;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

@Schema(description = "Corpo unico de listagem paginada")
public record PaginaResponse<T>(
        @Schema(description = "Itens da pagina pedida, na ordem da listagem")
        List<T> itens,

        @Schema(description = "Numero da pagina devolvida, a primeira e 0", example = "0")
        int pagina,

        @Schema(description = "Quantidade maxima de itens da pagina. Padrao 20, maximo 100", example = "20")
        int tamanho,

        @Schema(description = "Total de itens em todas as paginas", example = "42")
        long totalItens,

        @Schema(description = "Total de paginas para o tamanho usado", example = "3")
        int totalPaginas) {

    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> conversor) {
        return new PaginaResponse<>(
                pagina.getContent().stream().map(conversor).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
