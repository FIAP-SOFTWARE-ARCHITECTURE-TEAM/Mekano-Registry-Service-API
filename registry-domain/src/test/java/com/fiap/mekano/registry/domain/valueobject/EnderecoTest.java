package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnderecoTest {

    @Test
    void deveNormalizarUfECep() {
        Endereco endereco = new Endereco("Rua A", " 10 ", "  ", " Centro ", "Sao Paulo", "sp", "01001-000");

        assertThat(endereco.getUf()).isEqualTo("SP");
        assertThat(endereco.getCep()).isEqualTo("01001000");
        assertThat(endereco.getNumero()).isEqualTo("10");
        assertThat(endereco.getComplemento()).isNull();
        assertThat(endereco.getBairro()).isEqualTo("Centro");
    }

    @Test
    void deveRejeitarLogradouroVazio() {
        assertThatThrownBy(() -> new Endereco(" ", "10", null, "Centro", "Sao Paulo", "SP", "01001000"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarCidadeVazia() {
        assertThatThrownBy(() -> new Endereco("Rua A", "10", null, "Centro", " ", "SP", "01001000"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarUfInvalida() {
        assertThatThrownBy(() -> new Endereco("Rua A", "10", null, "Centro", "Sao Paulo", "SPX", "01001000"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveRejeitarCepInvalido() {
        assertThatThrownBy(() -> new Endereco("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "123"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }
}
