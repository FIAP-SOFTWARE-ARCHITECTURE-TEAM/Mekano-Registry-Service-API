package com.fiap.mekano.registry.rest.dto;

import java.util.List;

public record ServicoPageResponse(
        List<ServicoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
