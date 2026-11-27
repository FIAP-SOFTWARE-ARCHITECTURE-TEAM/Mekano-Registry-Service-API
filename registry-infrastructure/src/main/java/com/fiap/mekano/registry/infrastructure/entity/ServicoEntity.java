package com.fiap.mekano.registry.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "servicos")
@Getter
@Setter
@NoArgsConstructor
public class ServicoEntity extends BaseEntity {

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;
}
