package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void deveNormalizarEmailParaMinusculas() {
        assertThat(new Email("  Cliente.Exemplo@Mekano.COM.BR  ").getValue())
                .isEqualTo("cliente.exemplo@mekano.com.br");
    }

    @Test
    void deveRejeitarEmailInvalido() {
        assertThatThrownBy(() -> new Email("email-invalido"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarEmailNuloOuVazio() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
        assertThatThrownBy(() -> new Email(" "))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }
}
