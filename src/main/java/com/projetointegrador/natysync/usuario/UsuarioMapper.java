package com.projetointegrador.natysync.usuario;

import com.projetointegrador.natysync.usuario.dto.UsuarioResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse paraResposta(Usuario usuario);
}
