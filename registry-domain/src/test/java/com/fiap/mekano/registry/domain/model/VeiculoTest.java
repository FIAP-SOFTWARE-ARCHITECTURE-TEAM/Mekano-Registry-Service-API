package com.fiap.mekano.registry.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoTest {

    @Test
    void deveCriarVeiculoAtivoComPlacaNormalizada() {
        UUID clienteUuid = UUID.randomUUID();

        Veiculo veiculo = Veiculo.create(clienteUuid, "abc1d23", "Volkswagen", "Gol", 2020);

        assertThat(veiculo.getId()).isNotNull();
        assertThat(veiculo.getClienteUuid()).isEqualTo(clienteUuid);
        assertThat(veiculo.getPlaca().getValue()).isEqualTo("ABC1D23");
        assertThat(veiculo.getMarca()).isEqualTo("Volkswagen");
        assertThat(veiculo.getModelo()).isEqualTo("Gol");
        assertThat(veiculo.getAno()).isEqualTo(2020);
        assertThat(veiculo.getIsActive()).isTrue();
        assertThat(veiculo.getCreatedAt()).isNotNull();
    }

    @Test
    void deveReconstituirVeiculoInativo() {
        UUID id = UUID.randomUUID();
        UUID clienteUuid = UUID.randomUUID();

        Veiculo veiculo = Veiculo.reconstitute(
                id, clienteUuid, "ABC1234", "Fiat", "Uno", 2015,
                java.time.LocalDateTime.now(), Boolean.FALSE);

        assertThat(veiculo.getId()).isEqualTo(id);
        assertThat(veiculo.getClienteUuid()).isEqualTo(clienteUuid);
        assertThat(veiculo.getPlaca().getValue()).isEqualTo("ABC1234");
        assertThat(veiculo.getIsActive()).isFalse();
    }
}
