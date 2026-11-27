package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.infrastructure.entity.VeiculoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VeiculoPanacheRepository implements PanacheRepositoryBase<VeiculoEntity, Long> {
}
