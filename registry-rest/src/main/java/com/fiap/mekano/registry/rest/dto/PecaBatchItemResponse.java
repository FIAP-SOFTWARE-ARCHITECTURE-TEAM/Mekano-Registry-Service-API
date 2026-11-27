package com.fiap.mekano.registry.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PecaBatchItemResponse(
        UUID uuid,
        String codigo,
        String descricao,
        BigDecimal valorUnitario,
        Boolean isActive) {
}
