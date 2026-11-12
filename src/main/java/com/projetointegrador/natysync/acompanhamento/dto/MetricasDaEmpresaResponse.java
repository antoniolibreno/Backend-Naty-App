package com.projetointegrador.natysync.acompanhamento.dto;

import java.util.UUID;

public record MetricasDaEmpresaResponse(
        UUID empresaId,
        int totalIntegrantes,
        int adesaoPercentual,
        int percentualMedioDeConclusao,
        int concluiram,
        int avancando,
        int parados,
        int semInicio) {}
