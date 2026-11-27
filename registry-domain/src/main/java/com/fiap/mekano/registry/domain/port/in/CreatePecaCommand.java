package com.fiap.mekano.registry.domain.port.in;

import java.math.BigDecimal;

public record CreatePecaCommand(String codigo, String descricao, BigDecimal valorUnitario) {
}
