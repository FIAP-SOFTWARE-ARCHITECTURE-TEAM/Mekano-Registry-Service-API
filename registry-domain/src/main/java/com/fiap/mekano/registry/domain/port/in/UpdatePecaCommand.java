package com.fiap.mekano.registry.domain.port.in;

import java.math.BigDecimal;

public record UpdatePecaCommand(String codigo, String descricao, BigDecimal valorUnitario) {
}
