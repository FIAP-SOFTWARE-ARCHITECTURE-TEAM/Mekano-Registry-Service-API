package com.fiap.mekano.registry.rest.mapper;

import com.fiap.mekano.registry.domain.model.Servico;
import com.fiap.mekano.registry.domain.port.in.CreateServicoCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateServicoCommand;
import com.fiap.mekano.registry.rest.dto.CreateServicoRequest;
import com.fiap.mekano.registry.rest.dto.ServicoResponse;
import com.fiap.mekano.registry.rest.dto.UpdateServicoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface ServicoDtoMapper {

    CreateServicoCommand toCommand(CreateServicoRequest request);

    UpdateServicoCommand toCommand(UpdateServicoRequest request);

    @Mapping(target = "uuid", source = "id")
    ServicoResponse toResponse(Servico servico);
}
