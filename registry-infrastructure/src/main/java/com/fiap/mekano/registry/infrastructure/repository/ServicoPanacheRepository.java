package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.infrastructure.entity.ServicoEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicoPanacheRepository implements PanacheRepositoryBase<ServicoEntity, Long> {
}
