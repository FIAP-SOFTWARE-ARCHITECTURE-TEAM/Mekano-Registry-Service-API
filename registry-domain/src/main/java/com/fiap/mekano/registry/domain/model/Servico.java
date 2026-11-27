package com.fiap.mekano.registry.domain.model;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class Servico {

    private final UUID id;
    private final String nome;
    private final String descricao;
    private final BigDecimal valorUnitario;
    private final LocalDateTime createdAt;
    private final Boolean isActive;

    public static Servico create(String nome, String descricao, BigDecimal valorUnitario) {
        validar(nome, valorUnitario);
        return Servico.builder()
                .id(UUID.randomUUID())
                .nome(nome.strip())
                .descricao(descricao == null || descricao.isBlank() ? null : descricao.strip())
                .valorUnitario(valorUnitario)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public static Servico reconstitute(
            UUID id,
            String nome,
            String descricao,
            BigDecimal valorUnitario,
            LocalDateTime createdAt,
            Boolean isActive) {
        validar(nome, valorUnitario);
        return Servico.builder()
                .id(id)
                .nome(nome.strip())
                .descricao(descricao == null || descricao.isBlank() ? null : descricao.strip())
                .valorUnitario(valorUnitario)
                .createdAt(createdAt)
                .isActive(isActive == null || isActive)
                .build();
    }

    private static void validar(String nome, BigDecimal valorUnitario) {
        if (nome == null || nome.isBlank()) {
            throw new AppException(400, Messages.get("servico.nome.required"));
        }
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(400, Messages.get("servico.valor.invalid"));
        }
    }
}
