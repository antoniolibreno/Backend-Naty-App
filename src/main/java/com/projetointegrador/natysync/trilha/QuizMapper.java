package com.projetointegrador.natysync.trilha;

import com.projetointegrador.natysync.trilha.dto.AlternativaResponse;
import com.projetointegrador.natysync.trilha.dto.PerguntaResponse;
import com.projetointegrador.natysync.trilha.dto.QuizResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "atividadeId", source = "atividade.id")
    QuizResponse paraResposta(Quiz quiz);

    PerguntaResponse paraPergunta(Pergunta pergunta);

    AlternativaResponse paraAlternativa(Alternativa alternativa);
}
