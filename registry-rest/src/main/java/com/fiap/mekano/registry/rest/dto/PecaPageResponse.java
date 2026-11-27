package com.fiap.mekano.registry.rest.dto;

import java.util.List;

public record PecaPageResponse(
        List<PecaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
