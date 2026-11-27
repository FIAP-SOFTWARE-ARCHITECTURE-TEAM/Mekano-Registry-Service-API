package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefoneTest {

    @Test
    void deveAceitarCelularENormalizarParaDigitos() {
        assertThat(new Telefone("(11) 99999-9999").getValue()).isEqualTo("11999999999");
    }

    @Test
    void deveAceitarTelefoneFixo() {
        assertThat(new Telefone("1133334444").getValue()).isEqualTo("1133334444");
    }

    @Test
    void deveRejeitarTelefoneComTamanhoInvalido() {
        assertThatThrownBy(() -> new Telefone("119999999"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarDddInvalido() {
        assertThatThrownBy(() -> new Telefone("20999999999"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarTelefoneNulo() {
        assertThatThrownBy(() -> new Telefone(null))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }
}
