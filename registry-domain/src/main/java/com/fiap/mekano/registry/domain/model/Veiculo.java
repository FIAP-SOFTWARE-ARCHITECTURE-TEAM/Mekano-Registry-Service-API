package com.fiap.mekano.registry.domain.model;

import com.fiap.mekano.registry.domain.valueobject.PlacaVeiculo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de dominio Veiculo - POJO puro, sem anotacoes JPA.
 *
 * <p>O cliente e referenciado apenas pelo UUID do aggregate, sem FK de
 * dominio. A placa e validada pelo VO {@link PlacaVeiculo} e e imutavel apos a
 * criacao.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class Veiculo {

    private final UUID id;
    private final UUID clienteUuid;
    private final PlacaVeiculo placa;
    private final String marca;
    private final String modelo;
    private final Integer ano;
    private final LocalDateTime createdAt;
    private final Boolean isActive;

    public static Veiculo create(UUID clienteUuid, String placaValue, String marca, String modelo, Integer ano) {
        return Veiculo.builder()
                .id(UUID.randomUUID())
                .clienteUuid(clienteUuid)
                .placa(new PlacaVeiculo(placaValue))
                .marca(marca)
                .modelo(modelo)
                .ano(ano)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public static Veiculo reconstitute(
            UUID id,
            UUID clienteUuid,
            String placaValue,
            String marca,
            String modelo,
            Integer ano,
            LocalDateTime createdAt,
            Boolean isActive) {

        return Veiculo.builder()
                .id(id)
                .clienteUuid(clienteUuid)
                .placa(new PlacaVeiculo(placaValue))
                .marca(marca)
                .modelo(modelo)
                .ano(ano)
                .createdAt(createdAt)
                .isActive(isActive == null ? Boolean.TRUE : isActive)
                .build();
    }
}
