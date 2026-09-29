package com.projetointegrador.natysync.painel;

import com.projetointegrador.natysync.empresa.Empresa;
import com.projetointegrador.natysync.painel.dto.EmpresaResponse;
import java.time.ZoneId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    EmpresaResponse paraResposta(Empresa empresa);

    default String paraTexto(ZoneId fuso) {
        return fuso.getId();
    }
}
