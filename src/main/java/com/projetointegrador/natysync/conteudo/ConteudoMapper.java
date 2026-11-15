package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlteracaoDeConteudoResponse;
import com.projetointegrador.natysync.conteudo.dto.AlternativaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.PerguntaAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.QuizAdminResponse;
import com.projetointegrador.natysync.conteudo.dto.TrilhaAdminResponse;
import com.projetointegrador.natysync.trilha.Alternativa;
import com.projetointegrador.natysync.trilha.Pergunta;
import com.projetointegrador.natysync.trilha.Quiz;
import com.projetointegrador.natysync.trilha.Trilha;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConteudoMapper {

    TrilhaAdminResponse paraTrilha(Trilha trilha);

    AlteracaoDeConteudoResponse paraAlteracao(AlteracaoDeConteudo alteracao);

    @Mapping(target = "atividadeId", source = "atividade.id")
    QuizAdminResponse paraQuiz(Quiz quiz);

    PerguntaAdminResponse paraPergunta(Pergunta pergunta);

    AlternativaAdminResponse paraAlternativa(Alternativa alternativa);
}
