package com.fiap.mekano.registry.rest.mapper;

import com.fiap.mekano.registry.domain.model.Veiculo;
import com.fiap.mekano.registry.domain.port.in.CreateVeiculoCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateVeiculoCommand;
import com.fiap.mekano.registry.rest.dto.CreateVeiculoRequest;
import com.fiap.mekano.registry.rest.dto.UpdateVeiculoRequest;
import com.fiap.mekano.registry.rest.dto.VeiculoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapeamento entre DTOs REST e o modelo de dominio de Veiculo.
 */
@Mapper(componentModel = "cdi")
public interface VeiculoDtoMapper {

    CreateVeiculoCommand toCommand(CreateVeiculoRequest request);

    UpdateVeiculoCommand toCommand(UpdateVeiculoRequest request);

    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "placa", expression = "java(veiculo.getPlaca().getValue())")
    VeiculoResponse toResponse(Veiculo veiculo);
}
