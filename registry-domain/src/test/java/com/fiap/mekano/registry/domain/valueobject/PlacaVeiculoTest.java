package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlacaVeiculoTest {

    @Test
    void deveAceitarPlacaPadraoAntigo() {
        assertThat(new PlacaVeiculo("ABC1234").getValue()).isEqualTo("ABC1234");
    }

    @Test
    void deveAceitarPlacaMercosulENormalizar() {
        assertThat(new PlacaVeiculo(" abc1d23 ").getValue()).isEqualTo("ABC1D23");
    }

    @Test
    void deveRemoverHifenENormalizarCaixa() {
        assertThat(new PlacaVeiculo("abc-1234").getValue()).isEqualTo("ABC1234");
    }

    @Test
    void deveRejeitarPlacaInvalida() {
        assertThatThrownBy(() -> new PlacaVeiculo("AB12345"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarPlacaNulaOuVazia() {
        assertThatThrownBy(() -> new PlacaVeiculo(null))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
        assertThatThrownBy(() -> new PlacaVeiculo(" "))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }
}
