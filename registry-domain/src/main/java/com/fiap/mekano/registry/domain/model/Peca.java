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
public class Peca {

    private final UUID id;
    private final String codigo;
    private final String descricao;
    private final BigDecimal valorUnitario;
    private final LocalDateTime createdAt;
    private final Boolean isActive;

    public static Peca create(String codigo, String descricao, BigDecimal valorUnitario) {
        validar(codigo, descricao, valorUnitario);
        return Peca.builder()
                .id(UUID.randomUUID())
                .codigo(codigo.strip())
                .descricao(descricao.strip())
                .valorUnitario(valorUnitario)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public static Peca reconstitute(
            UUID id,
            String codigo,
            String descricao,
            BigDecimal valorUnitario,
            LocalDateTime createdAt,
            Boolean isActive) {
        validar(codigo, descricao, valorUnitario);
        return Peca.builder()
                .id(id)
                .codigo(codigo.strip())
                .descricao(descricao.strip())
                .valorUnitario(valorUnitario)
                .createdAt(createdAt)
                .isActive(isActive == null || isActive)
                .build();
    }

    private static void validar(String codigo, String descricao, BigDecimal valorUnitario) {
        if (codigo == null || codigo.isBlank()) {
            throw new AppException(400, Messages.get("peca.codigo.required"));
        }
        if (descricao == null || descricao.isBlank()) {
            throw new AppException(400, Messages.get("peca.descricao.required"));
        }
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(400, Messages.get("peca.valor.invalid"));
        }
    }
}
