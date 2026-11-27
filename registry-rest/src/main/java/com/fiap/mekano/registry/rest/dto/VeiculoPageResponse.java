package com.fiap.mekano.registry.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta paginada de veiculos")
public record VeiculoPageResponse(
        List<VeiculoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
