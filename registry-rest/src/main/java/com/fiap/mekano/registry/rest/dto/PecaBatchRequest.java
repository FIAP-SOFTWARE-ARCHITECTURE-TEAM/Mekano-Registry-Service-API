package com.fiap.mekano.registry.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "UUIDs das pecas consultadas em lote")
public record PecaBatchRequest(
        @NotEmpty(message = "A lista de UUIDs nao pode estar vazia")
        @Size(max = 100, message = "O lote pode conter no maximo 100 UUIDs")
        @Schema(required = true, example = "[\"55555555-5555-5555-5555-555555555555\"]")
        List<@NotNull UUID> pecaUuids) {
}
