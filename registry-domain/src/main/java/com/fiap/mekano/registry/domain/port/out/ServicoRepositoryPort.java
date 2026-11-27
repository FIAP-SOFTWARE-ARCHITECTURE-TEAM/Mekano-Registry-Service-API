package com.fiap.mekano.registry.domain.port.out;

import com.fiap.mekano.registry.domain.model.Servico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicoRepositoryPort {

    Servico create(Servico servico);

    Servico update(Servico servico);

    Optional<Servico> findById(UUID id);

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, UUID id);

    List<Servico> findAll(int page, int size, String sort, String nome, Boolean isActive);

    long countAll(String nome, Boolean isActive);

    void markAsDeleted(UUID id);

    void reactivate(UUID id);
}
