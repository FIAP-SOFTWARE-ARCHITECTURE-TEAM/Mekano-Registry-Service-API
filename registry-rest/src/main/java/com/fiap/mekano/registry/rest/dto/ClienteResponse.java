package com.fiap.mekano.registry.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados do cliente")
public record ClienteResponse(
        UUID uuid,
        String nome,
        String cpf,
        String email,
        String telefone,
        EnderecoResponse endereco,
        Boolean isActive,
        LocalDateTime createdAt
) {
}
