package com.projetointegrador.natysync.shared.pagina;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

@Schema(description = "Listagem paginada")
public record PaginaResponse<T>(
        @Schema(description = "Itens da página") List<T> itens,

        @Schema(description = "Página, a primeira é 0", example = "0")
        int pagina,

        @Schema(description = "Itens por página. Padrão 20, máximo 100", example = "20")
        int tamanho,

        @Schema(description = "Total de itens", example = "42")
        long totalItens,

        @Schema(description = "Total de páginas", example = "3")
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
