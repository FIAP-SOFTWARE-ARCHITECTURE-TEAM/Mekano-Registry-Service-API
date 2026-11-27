package com.fiap.mekano.registry.domain.port.out;

import com.fiap.mekano.registry.domain.model.Veiculo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistencia de Veiculo.
 */
public interface VeiculoRepositoryPort {

    Veiculo create(Veiculo veiculo);

    Veiculo update(Veiculo veiculo);

    Optional<Veiculo> findById(UUID id);

    Optional<Veiculo> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    List<Veiculo> findAll(int page, int size, String sort, UUID clienteUuid, String placa, Boolean isActive);

    long countAll(UUID clienteUuid, String placa, Boolean isActive);

    void markAsDeleted(UUID id);

    void reactivate(UUID id);
}
