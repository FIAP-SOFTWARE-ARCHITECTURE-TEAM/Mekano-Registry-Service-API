package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.infrastructure.entity.ClienteEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientePanacheRepository implements PanacheRepositoryBase<ClienteEntity, Long> {
}
