package com.fiap.mekano.registry.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "veiculos")
@Getter
@Setter
@NoArgsConstructor
public class VeiculoEntity extends BaseEntity {

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "cliente_uuid", nullable = false)
    private UUID clienteUuid;

    @Column(name = "placa", nullable = false, length = 8)
    private String placa;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "ano")
    private Integer ano;
}
