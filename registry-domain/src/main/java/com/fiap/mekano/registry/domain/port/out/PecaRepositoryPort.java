package com.fiap.mekano.registry.domain.port.out;

import com.fiap.mekano.registry.domain.model.Peca;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PecaRepositoryPort {

    Peca create(Peca peca);

    Peca update(Peca peca);

    Optional<Peca> findById(UUID id);

    List<Peca> findByIds(Collection<UUID> ids);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, UUID id);

    List<Peca> findAll(int page, int size, String sort, String codigo, String descricao, Boolean isActive);

    long countAll(String codigo, String descricao, Boolean isActive);

    void markAsDeleted(UUID id);

    void reactivate(UUID id);
}
