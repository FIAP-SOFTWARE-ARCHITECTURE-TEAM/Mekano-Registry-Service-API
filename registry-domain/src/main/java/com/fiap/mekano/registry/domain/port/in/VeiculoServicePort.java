package com.fiap.mekano.registry.domain.port.in;

import com.fiap.mekano.registry.domain.model.Veiculo;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de casos de uso de Veiculo.
 */
public interface VeiculoServicePort {

    Veiculo execute(CreateVeiculoCommand command);

    Veiculo update(UUID id, UpdateVeiculoCommand command);

    Veiculo findById(UUID id);

    List<Veiculo> findAll(int page, int size, String sort, UUID clienteUuid, String placa, Boolean isActive);

    long countAll(UUID clienteUuid, String placa, Boolean isActive);

    void delete(UUID id);

    void reactivate(UUID id);
}
