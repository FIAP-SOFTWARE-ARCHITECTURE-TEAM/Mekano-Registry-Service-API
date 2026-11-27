package com.fiap.mekano.registry.rest.dto;

import java.util.List;
import java.util.UUID;

public record PecaBatchResponse(
        List<PecaBatchItemResponse> pecas,
        List<UUID> naoEncontradas) {
}
