package com.fiap.mekano.registry.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de uma peca do catalogo, sem informacoes de estoque")
public record PecaResponse(
        UUID uuid,
        String codigo,
        String descricao,
        BigDecimal valorUnitario,
        Boolean isActive,
        LocalDateTime createdAt) {
}
