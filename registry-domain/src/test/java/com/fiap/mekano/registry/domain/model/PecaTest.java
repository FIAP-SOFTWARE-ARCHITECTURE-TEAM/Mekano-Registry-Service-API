package com.fiap.mekano.registry.domain.model;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PecaTest {

    @Test
    void deveCriarPecaDeCatalogoSemDadosDeEstoque() {
        Peca peca = Peca.create(" PEA-001 ", " Filtro de oleo ", new BigDecimal("45.90"));

        assertThat(peca.getId()).isNotNull();
        assertThat(peca.getCodigo()).isEqualTo("PEA-001");
        assertThat(peca.getDescricao()).isEqualTo("Filtro de oleo");
        assertThat(peca.getValorUnitario()).isEqualByComparingTo("45.90");
        assertThat(peca.getIsActive()).isTrue();
        assertThat(peca.getCreatedAt()).isNotNull();
    }

    @Test
    void deveRejeitarValorZeroOuNegativo() {
        assertThatThrownBy(() -> Peca.create("PEA-001", "Filtro", BigDecimal.ZERO))
                .isInstanceOfSatisfying(AppException.class, exception -> assertThat(exception.getStatus()).isEqualTo(400));
        assertThatThrownBy(() -> Peca.create("PEA-001", "Filtro", new BigDecimal("-1.00")))
                .isInstanceOf(AppException.class);
    }

    @Test
    void deveReconstituirPecaInativa() {
        UUID id = UUID.randomUUID();
        Peca peca = Peca.reconstitute(
                id, "PEA-001", "Filtro", new BigDecimal("45.90"), LocalDateTime.now(), false);

        assertThat(peca.getId()).isEqualTo(id);
        assertThat(peca.getIsActive()).isFalse();
    }
}
