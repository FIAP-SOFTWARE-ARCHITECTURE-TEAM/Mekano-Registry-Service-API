package com.fiap.mekano.registry.domain.model;

import com.fiap.mekano.registry.domain.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private static final String CPF_VALIDO = "52998224725";

    @Test
    void deveCriarClienteAtivoComDadosNormalizados() {
        Cliente cliente = Cliente.create(
                "  Maria Silva  ",
                CPF_VALIDO,
                " Maria@Example.com ",
                "(11) 99999-9999",
                "Rua A",
                "10",
                null,
                "Centro",
                "Sao Paulo",
                "sp",
                "01001-000");

        assertThat(cliente.getId()).isNotNull();
        assertThat(cliente.getNome()).isEqualTo("Maria Silva");
        assertThat(cliente.getCpf().getValue()).isEqualTo(CPF_VALIDO);
        assertThat(cliente.getEmail().getValue()).isEqualTo("maria@example.com");
        assertThat(cliente.getTelefone().getValue()).isEqualTo("11999999999");
        assertThat(cliente.getEndereco().getUf()).isEqualTo("SP");
        assertThat(cliente.getEndereco().getCep()).isEqualTo("01001000");
        assertThat(cliente.getIsActive()).isTrue();
        assertThat(cliente.getCreatedAt()).isNotNull();
    }

    @Test
    void deveAtualizarDadosMantendoCpf() {
        Cliente cliente = Cliente.create(
                "Maria", CPF_VALIDO, "maria@example.com", "11999999999",
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000");

        cliente.atualizar(
                "Joao", "joao@example.com", "11988887777",
                "Rua B", "20", "Apto 1", "Bairro Novo", "Campinas", "SP", "13010000");

        assertThat(cliente.getNome()).isEqualTo("Joao");
        assertThat(cliente.getEmail().getValue()).isEqualTo("joao@example.com");
        assertThat(cliente.getTelefone().getValue()).isEqualTo("11988887777");
        assertThat(cliente.getEndereco().getLogradouro()).isEqualTo("Rua B");
        assertThat(cliente.getEndereco().getComplemento()).isEqualTo("Apto 1");
        assertThat(cliente.getCpf().getValue()).isEqualTo(CPF_VALIDO);
    }

    @Test
    void deveRejeitarNomeVazioNaCriacao() {
        assertThatThrownBy(() -> Cliente.create(
                "  ", CPF_VALIDO, "maria@example.com", null,
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000"))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(400));
    }

    @Test
    void deveReconstituirClienteInativo() {
        Cliente cliente = Cliente.reconstitute(
                java.util.UUID.randomUUID(), "Maria", CPF_VALIDO, "maria@example.com", null,
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000",
                java.time.LocalDateTime.now(), Boolean.FALSE);

        assertThat(cliente.getIsActive()).isFalse();
    }
}
