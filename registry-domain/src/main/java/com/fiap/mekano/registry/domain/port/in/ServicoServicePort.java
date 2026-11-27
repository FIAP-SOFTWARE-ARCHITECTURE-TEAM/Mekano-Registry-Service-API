package com.fiap.mekano.registry.domain.port.in;

import com.fiap.mekano.registry.domain.model.Servico;

import java.util.List;
import java.util.UUID;

public interface ServicoServicePort {

    Servico create(CreateServicoCommand command);

    Servico update(UUID id, UpdateServicoCommand command);

    Servico findById(UUID id);

    List<Servico> findAll(int page, int size, String sort, String nome, Boolean isActive);

    long countAll(String nome, Boolean isActive);

    void delete(UUID id);

    void reactivate(UUID id);
}
