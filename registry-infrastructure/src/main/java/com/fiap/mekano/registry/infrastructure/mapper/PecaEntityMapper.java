package com.fiap.mekano.registry.infrastructure.mapper;

import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.infrastructure.entity.PecaEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PecaEntityMapper {

    public PecaEntity toEntity(Peca peca) {
        PecaEntity entity = new PecaEntity();
        entity.setUuid(peca.getId());
        entity.setCodigo(peca.getCodigo());
        entity.setDescricao(peca.getDescricao());
        entity.setValorUnitario(peca.getValorUnitario());
        entity.setCreatedAt(peca.getCreatedAt());
        entity.setIsActive(peca.getIsActive());
        return entity;
    }

    public Peca toDomain(PecaEntity entity) {
        return Peca.reconstitute(
                entity.getUuid(),
                entity.getCodigo(),
                entity.getDescricao(),
                entity.getValorUnitario(),
                entity.getCreatedAt(),
                entity.getIsActive());
    }

    public void updateEntity(Peca peca, PecaEntity entity) {
        entity.setCodigo(peca.getCodigo());
        entity.setDescricao(peca.getDescricao());
        entity.setValorUnitario(peca.getValorUnitario());
    }
}
