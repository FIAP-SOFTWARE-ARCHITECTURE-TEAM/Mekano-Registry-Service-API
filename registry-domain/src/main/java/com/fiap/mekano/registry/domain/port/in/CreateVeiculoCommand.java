package com.fiap.mekano.registry.domain.port.in;

import java.util.UUID;

/**
 * Comando de criacao de veiculo.
 */
public record CreateVeiculoCommand(
        UUID clienteUuid,
        String placa,
        String marca,
        String modelo,
        Integer ano
) {
}
