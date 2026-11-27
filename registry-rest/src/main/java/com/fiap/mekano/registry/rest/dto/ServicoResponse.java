package com.fiap.mekano.registry.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de um servico")
public record ServicoResponse(
        UUID uuid,
        String nome,
        String descricao,
        BigDecimal valorUnitario,
        Boolean isActive,
        LocalDateTime createdAt) {
}
