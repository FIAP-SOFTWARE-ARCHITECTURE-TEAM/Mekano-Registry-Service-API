package com.fiap.mekano.registry.domain.port.in;

/**
 * Comando de atualizacao de veiculo. A placa e imutavel e o cliente nao pode
 * ser trocado apos a criacao.
 */
public record UpdateVeiculoCommand(
        String marca,
        String modelo,
        Integer ano
) {
}
