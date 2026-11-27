package com.fiap.mekano.registry.infrastructure.mapper;

import com.fiap.mekano.registry.domain.model.Servico;
import com.fiap.mekano.registry.infrastructure.entity.ServicoEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServicoEntityMapper {

    public ServicoEntity toEntity(Servico servico) {
        ServicoEntity entity = new ServicoEntity();
        entity.setUuid(servico.getId());
        entity.setNome(servico.getNome());
        entity.setDescricao(servico.getDescricao());
        entity.setValorUnitario(servico.getValorUnitario());
        entity.setCreatedAt(servico.getCreatedAt());
        entity.setIsActive(servico.getIsActive());
        return entity;
    }

    public Servico toDomain(ServicoEntity entity) {
        return Servico.reconstitute(
                entity.getUuid(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getValorUnitario(),
                entity.getCreatedAt(),
                entity.getIsActive());
    }

    public void updateEntity(Servico servico, ServicoEntity entity) {
        entity.setNome(servico.getNome());
        entity.setDescricao(servico.getDescricao());
        entity.setValorUnitario(servico.getValorUnitario());
    }
}
