package com.fiap.mekano.registry.infrastructure.mapper;

import com.fiap.mekano.registry.domain.model.Veiculo;
import com.fiap.mekano.registry.infrastructure.entity.VeiculoEntity;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapeamento entre Veiculo (dominio) e VeiculoEntity (JPA).
 */
@ApplicationScoped
public class VeiculoEntityMapper {

    public VeiculoEntity toEntity(Veiculo veiculo) {
        if (veiculo == null) {
            return null;
        }
        VeiculoEntity entity = new VeiculoEntity();
        entity.setUuid(veiculo.getId());
        entity.setClienteUuid(veiculo.getClienteUuid());
        entity.setPlaca(veiculo.getPlaca().getValue());
        entity.setMarca(veiculo.getMarca());
        entity.setModelo(veiculo.getModelo());
        entity.setAno(veiculo.getAno());
        entity.setCreatedAt(veiculo.getCreatedAt());
        entity.setIsActive(veiculo.getIsActive());
        return entity;
    }

    public Veiculo toDomain(VeiculoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Veiculo.reconstitute(
                entity.getUuid(),
                entity.getClienteUuid(),
                entity.getPlaca(),
                entity.getMarca(),
                entity.getModelo(),
                entity.getAno(),
                entity.getCreatedAt(),
                entity.getIsActive());
    }

    public void updateEntity(Veiculo veiculo, VeiculoEntity entity) {
        if (veiculo == null || entity == null) {
            return;
        }
        entity.setMarca(veiculo.getMarca());
        entity.setModelo(veiculo.getModelo());
        entity.setAno(veiculo.getAno());
    }
}
