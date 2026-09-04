package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.trilha.dto.AtividadeDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.AtividadeResumoResponse;
import com.projetointegrador.natysync.trilha.dto.ModuloResponse;
import com.projetointegrador.natysync.trilha.dto.TrilhaDetalheResponse;
import com.projetointegrador.natysync.trilha.dto.TrilhaResumoResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrilhaMapper {

    TrilhaResumoResponse paraResumo(Trilha trilha);

    List<TrilhaResumoResponse> paraResumos(List<Trilha> trilhas);

    TrilhaDetalheResponse paraDetalhe(Trilha trilha);

    ModuloResponse paraModulo(Modulo modulo);

    AtividadeResumoResponse paraAtividadeResumo(Atividade atividade);

    @Mapping(target = "possuiQuiz", expression = "java(atividade.getQuiz() != null)")
    AtividadeDetalheResponse paraAtividadeDetalhe(Atividade atividade);
}
