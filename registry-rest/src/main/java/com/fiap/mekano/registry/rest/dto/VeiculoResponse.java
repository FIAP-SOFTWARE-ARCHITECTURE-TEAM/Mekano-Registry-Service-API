package com.fiap.mekano.registry.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados do veiculo")
public record VeiculoResponse(
        UUID uuid,
        UUID clienteUuid,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Boolean isActive,
        LocalDateTime createdAt
) {
}
