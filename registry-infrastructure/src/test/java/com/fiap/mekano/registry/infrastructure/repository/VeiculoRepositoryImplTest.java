package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.model.Veiculo;
import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import com.fiap.mekano.registry.domain.port.out.VeiculoRepositoryPort;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class VeiculoRepositoryImplTest {

    private static final String CPF = "52998224725";
    private static final String CPF_OUTRO = "11144477735";

    @Inject
    VeiculoRepositoryPort veiculoRepository;

    @Inject
    ClienteRepositoryPort clienteRepository;

    @Test
    void deveCriarBuscarPorIdEPlaca() {
        UUID clienteUuid = cliente("Maria", CPF).getId();

        Veiculo salvo = veiculoRepository.create(
                Veiculo.create(clienteUuid, "abc1d23", "Volkswagen", "Gol", 2020));

        assertThat(salvo.getId()).isNotNull();
        assertThat(veiculoRepository.findById(salvo.getId()))
                .isPresent()
                .get()
                .satisfies(encontrado -> {
                    assertThat(encontrado.getPlaca().getValue()).isEqualTo("ABC1D23");
                    assertThat(encontrado.getClienteUuid()).isEqualTo(clienteUuid);
                    assertThat(encontrado.getIsActive()).isTrue();
                });
        assertThat(veiculoRepository.findByPlaca("abc-1d23")).isPresent();
        assertThat(veiculoRepository.existsByPlaca("ABC1D23")).isTrue();
    }

    @Test
    void deveFiltrarPorClientePlacaEStatus() {
        UUID mariaUuid = cliente("Maria", CPF).getId();
        UUID joaoUuid = cliente("Joao", CPF_OUTRO).getId();

        Veiculo gol = veiculoRepository.create(
                Veiculo.create(mariaUuid, "ABC1D23", "Volkswagen", "Gol", 2020));
        veiculoRepository.create(
                Veiculo.create(mariaUuid, "XYZ9876", "Fiat", "Uno", 2015));
        veiculoRepository.create(
                Veiculo.create(joaoUuid, "DEF5678", "Ford", "Ka", 2019));
        veiculoRepository.markAsDeleted(gol.getId());

        assertThat(veiculoRepository.findAll(0, 20, "placa,asc", mariaUuid, null, true))
                .extracting(veiculo -> veiculo.getPlaca().getValue())
                .containsExactly("XYZ9876");
        assertThat(veiculoRepository.countAll(mariaUuid, null, true)).isEqualTo(1);

        assertThat(veiculoRepository.findAll(0, 20, "placa,asc", null, "abc", null))
                .extracting(veiculo -> veiculo.getPlaca().getValue())
                .containsExactly("ABC1D23");

        assertThat(veiculoRepository.findAll(0, 20, "placa,asc", null, null, false))
                .extracting(veiculo -> veiculo.getPlaca().getValue())
                .containsExactly("ABC1D23");
        assertThat(veiculoRepository.countAll(null, null, true)).isEqualTo(2);
    }

    @Test
    void deveAtualizarMarcaModeloEAno() {
        UUID clienteUuid = cliente("Maria", CPF).getId();
        Veiculo salvo = veiculoRepository.create(
                Veiculo.create(clienteUuid, "ABC1D23", "Volkswagen", "Gol", 2020));

        Veiculo atualizado = Veiculo.reconstitute(
                salvo.getId(), salvo.getClienteUuid(), salvo.getPlaca().getValue(),
                "Fiat", "Uno", 2015, salvo.getCreatedAt(), salvo.getIsActive());

        Veiculo resultado = veiculoRepository.update(atualizado);

        assertThat(resultado.getMarca()).isEqualTo("Fiat");
        assertThat(resultado.getModelo()).isEqualTo("Uno");
        assertThat(resultado.getAno()).isEqualTo(2015);
        assertThat(resultado.getPlaca().getValue()).isEqualTo("ABC1D23");
        assertThat(resultado.getClienteUuid()).isEqualTo(clienteUuid);
    }

    @Test
    void deveExecutarSoftDeleteEReativacao() {
        UUID clienteUuid = cliente("Maria", CPF).getId();
        Veiculo salvo = veiculoRepository.create(
                Veiculo.create(clienteUuid, "ABC1D23", "Volkswagen", "Gol", 2020));

        veiculoRepository.markAsDeleted(salvo.getId());

        assertThat(veiculoRepository.existsByPlaca("ABC1D23")).isFalse();
        assertThat(veiculoRepository.findByPlaca("ABC1D23")).isEmpty();
        assertThat(veiculoRepository.findById(salvo.getId()))
                .get()
                .extracting(Veiculo::getIsActive)
                .isEqualTo(false);

        veiculoRepository.reactivate(salvo.getId());

        assertThat(veiculoRepository.existsByPlaca("ABC1D23")).isTrue();
        assertThat(veiculoRepository.findById(salvo.getId()))
                .get()
                .extracting(Veiculo::getIsActive)
                .isEqualTo(true);
    }

    private Cliente cliente(String nome, String cpf) {
        return clienteRepository.create(Cliente.create(
                nome, cpf, "cliente@example.com", "11999999999",
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000"));
    }
}
