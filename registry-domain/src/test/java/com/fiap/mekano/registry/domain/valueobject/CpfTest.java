package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @Test
    void deveAceitarCpfValidoENormalizarParaDigitos() {
        assertThat(new Cpf("529.982.247-25").getValue()).isEqualTo("52998224725");
        assertThat(new Cpf("11144477735").getValue()).isEqualTo("11144477735");
    }

    @Test
    void deveRejeitarCpfNuloOuVazio() {
        assertThatThrownBy(() -> new Cpf(null))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
        assertThatThrownBy(() -> new Cpf("   "))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarCpfComQuantidadeInvalidaDeDigitos() {
        assertThatThrownBy(() -> new Cpf("1234567890"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarCpfComDigitosRepetidos() {
        assertThatThrownBy(() -> new Cpf("11111111111"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorInvalido() {
        assertThatThrownBy(() -> new Cpf("52998224724"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }
}
