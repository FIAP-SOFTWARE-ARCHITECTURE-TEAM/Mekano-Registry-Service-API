package com.fiap.mekano.registry.domain.model;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicoTest {

    @Test
    void deveCriarServicoComDescricaoOpcional() {
        Servico servico = Servico.create(" Troca de oleo ", null, new BigDecimal("150.00"));

        assertThat(servico.getId()).isNotNull();
        assertThat(servico.getNome()).isEqualTo("Troca de oleo");
        assertThat(servico.getDescricao()).isNull();
        assertThat(servico.getValorUnitario()).isEqualByComparingTo("150.00");
        assertThat(servico.getIsActive()).isTrue();
    }

    @Test
    void deveRejeitarValorNaoPositivo() {
        assertThatThrownBy(() -> Servico.create("Troca de oleo", null, BigDecimal.ZERO))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> Servico.create(" ", null, BigDecimal.ONE))
                .isInstanceOf(AppException.class);
    }
}
