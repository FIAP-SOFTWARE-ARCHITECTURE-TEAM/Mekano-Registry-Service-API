package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.infrastructure.entity.PecaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PecaPanacheRepository implements PanacheRepositoryBase<PecaEntity, Long> {
}
