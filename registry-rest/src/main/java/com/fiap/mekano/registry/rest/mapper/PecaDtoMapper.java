package com.fiap.mekano.registry.rest.mapper;

import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.domain.port.in.CreatePecaCommand;
import com.fiap.mekano.registry.domain.port.in.UpdatePecaCommand;
import com.fiap.mekano.registry.rest.dto.CreatePecaRequest;
import com.fiap.mekano.registry.rest.dto.PecaBatchItemResponse;
import com.fiap.mekano.registry.rest.dto.PecaResponse;
import com.fiap.mekano.registry.rest.dto.UpdatePecaRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface PecaDtoMapper {

    CreatePecaCommand toCommand(CreatePecaRequest request);

    UpdatePecaCommand toCommand(UpdatePecaRequest request);

    @Mapping(target = "uuid", source = "id")
    PecaResponse toResponse(Peca peca);

    @Mapping(target = "uuid", source = "id")
    PecaBatchItemResponse toBatchItem(Peca peca);
}
