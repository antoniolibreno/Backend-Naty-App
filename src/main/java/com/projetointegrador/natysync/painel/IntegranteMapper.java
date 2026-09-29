package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.painel.dto.IntegranteResponse;
import com.projetointegrador.natysync.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IntegranteMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    IntegranteResponse paraResposta(Usuario usuario);
}
