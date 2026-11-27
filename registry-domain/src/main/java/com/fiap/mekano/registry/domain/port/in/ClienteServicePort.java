package com.fiap.mekano.registry.domain.port.in;

import com.fiap.mekano.registry.domain.model.Cliente;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de casos de uso de Cliente.
 */
public interface ClienteServicePort {

    Cliente execute(CreateClienteCommand command);

    Cliente update(UUID id, UpdateClienteCommand command);

    Cliente findById(UUID id);

    List<Cliente> findAll(int page, int size, String sort, String nome, Boolean isActive);

    long countAll(String nome, Boolean isActive);

    void delete(UUID id);

    void reactivate(UUID id);
}
