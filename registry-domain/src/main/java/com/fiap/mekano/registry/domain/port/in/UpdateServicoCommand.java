package com.fiap.mekano.registry.domain.port.in;

import java.math.BigDecimal;

public record UpdateServicoCommand(String nome, String descricao, BigDecimal valorUnitario) {
}
