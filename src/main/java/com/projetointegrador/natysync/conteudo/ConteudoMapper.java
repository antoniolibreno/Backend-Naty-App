package com.projetointegrador.natysync.conteudo;

import com.projetointegrador.natysync.conteudo.dto.AlteracaoDeConteudoResponse;
import com.projetointegrador.natysync.conteudo.dto.TrilhaAdminResponse;
import com.projetointegrador.natysync.trilha.Trilha;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConteudoMapper {

    TrilhaAdminResponse paraTrilha(Trilha trilha);

    AlteracaoDeConteudoResponse paraAlteracao(AlteracaoDeConteudo alteracao);
}
