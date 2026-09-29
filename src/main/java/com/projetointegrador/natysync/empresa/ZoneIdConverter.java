package com.projetointegrador.natysync.empresa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.ZoneId;

@Converter
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

    @Override
    public String convertToDatabaseColumn(ZoneId fuso) {
        return fuso == null ? null : fuso.getId();
    }

    @Override
    public ZoneId convertToEntityAttribute(String identificador) {
        return identificador == null ? null : ZoneId.of(identificador);
    }
}
