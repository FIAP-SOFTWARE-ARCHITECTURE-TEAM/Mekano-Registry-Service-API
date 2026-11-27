package com.fiap.mekano.registry.domain.port.out;

import com.fiap.mekano.registry.domain.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistencia de Cliente.
 */
public interface ClienteRepositoryPort {

    Cliente create(Cliente cliente);

    Cliente update(Cliente cliente);

    Optional<Cliente> findById(UUID id);

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Cliente> findAll(int page, int size, String sort, String nome, Boolean isActive);

    long countAll(String nome, Boolean isActive);

    void markAsDeleted(UUID id);

    void reactivate(UUID id);
}
