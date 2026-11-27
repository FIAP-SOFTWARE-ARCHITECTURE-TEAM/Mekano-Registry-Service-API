package com.fiap.mekano.registry.domain.port.in;

import com.fiap.mekano.registry.domain.model.Peca;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PecaServicePort {

    Peca create(CreatePecaCommand command);

    Peca update(UUID id, UpdatePecaCommand command);

    Peca findById(UUID id);

    List<Peca> findByIds(Collection<UUID> ids);

    List<Peca> findAll(int page, int size, String sort, String codigo, String descricao, Boolean isActive);

    long countAll(String codigo, String descricao, Boolean isActive);

    void delete(UUID id);

    void reactivate(UUID id);
}
