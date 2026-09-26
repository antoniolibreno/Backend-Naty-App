package com.projetointegrador.natysync.empresa;

import com.projetointegrador.natysync.empresa.dto.EmpresaResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {
    EmpresaResponse paraResposta(Empresa empresa);
}
