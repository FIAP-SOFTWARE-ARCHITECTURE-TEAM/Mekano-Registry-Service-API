package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class ClienteRepositoryImplTest {

    private static final String CPF_MARIA = "52998224725";
    private static final String CPF_JOAO = "11144477735";
    private static final String CPF_ANA = "12345678909";

    @Inject
    ClienteRepositoryPort clienteRepository;

    @Test
    void deveCriarBuscarPorIdECpf() {
        Cliente salvo = clienteRepository.create(cliente("Maria", CPF_MARIA));

        assertThat(salvo.getId()).isNotNull();
        assertThat(clienteRepository.findById(salvo.getId()))
                .isPresent()
                .get()
                .satisfies(encontrado -> {
                    assertThat(encontrado.getNome()).isEqualTo("Maria");
                    assertThat(encontrado.getCpf().getValue()).isEqualTo(CPF_MARIA);
                    assertThat(encontrado.getIsActive()).isTrue();
                });
        assertThat(clienteRepository.findByCpf(CPF_MARIA)).isPresent();
        assertThat(clienteRepository.existsByCpf(CPF_MARIA)).isTrue();
    }

    @Test
    void deveFiltrarPorNomeEStatusComPaginacao() {
        clienteRepository.create(cliente("Maria Silva", CPF_MARIA));
        clienteRepository.create(cliente("Joao Souza", CPF_JOAO));
        Cliente ana = clienteRepository.create(cliente("Ana Maria", CPF_ANA));
        clienteRepository.markAsDeleted(ana.getId());

        List<Cliente> ativosComMaria = clienteRepository.findAll(0, 20, "nome,asc", "maria", true);
        assertThat(ativosComMaria)
                .extracting(Cliente::getNome)
                .containsExactly("Maria Silva");
        assertThat(clienteRepository.countAll("maria", true)).isEqualTo(1);

        List<Cliente> inativos = clienteRepository.findAll(0, 20, "nome,asc", null, false);
        assertThat(inativos)
                .extracting(Cliente::getNome)
                .containsExactly("Ana Maria");
        assertThat(clienteRepository.countAll(null, false)).isEqualTo(1);

        List<Cliente> primeiraPagina = clienteRepository.findAll(0, 2, "nome,asc", null, true);
        assertThat(primeiraPagina).hasSize(2);
        assertThat(clienteRepository.countAll(null, true)).isEqualTo(2);
    }

    @Test
    void deveAtualizarDadosDoCliente() {
        Cliente salvo = clienteRepository.create(cliente("Maria", CPF_MARIA));

        salvo.atualizar(
                "Maria Atualizada", "maria.nova@example.com", "11988887777",
                "Rua B", "20", "Apto 1", "Centro", "Campinas", "SP", "13010000");

        Cliente atualizado = clienteRepository.update(salvo);

        assertThat(atualizado.getNome()).isEqualTo("Maria Atualizada");
        assertThat(atualizado.getEmail().getValue()).isEqualTo("maria.nova@example.com");
        assertThat(atualizado.getCpf().getValue()).isEqualTo(CPF_MARIA);
        assertThat(atualizado.getEndereco().getCidade()).isEqualTo("Campinas");
    }

    @Test
    void deveExecutarSoftDeleteEReativacao() {
        Cliente salvo = clienteRepository.create(cliente("Maria", CPF_MARIA));

        clienteRepository.markAsDeleted(salvo.getId());

        assertThat(clienteRepository.existsByCpf(CPF_MARIA)).isFalse();
        assertThat(clienteRepository.findByCpf(CPF_MARIA)).isEmpty();
        assertThat(clienteRepository.findById(salvo.getId()))
                .get()
                .extracting(Cliente::getIsActive)
                .isEqualTo(false);

        clienteRepository.reactivate(salvo.getId());

        assertThat(clienteRepository.existsByCpf(CPF_MARIA)).isTrue();
        assertThat(clienteRepository.findById(salvo.getId()))
                .get()
                .extracting(Cliente::getIsActive)
                .isEqualTo(true);
    }

    @Test
    void devePermitirReutilizarCpfAposSoftDelete() {
        Cliente primeiro = clienteRepository.create(cliente("Maria", CPF_MARIA));
        clienteRepository.markAsDeleted(primeiro.getId());

        Cliente segundo = clienteRepository.create(cliente("Maria Nova", CPF_MARIA));

        assertThat(segundo.getId()).isNotEqualTo(primeiro.getId());
        assertThat(clienteRepository.findAll(0, 20, "nome,asc", null, true))
                .extracting(Cliente::getNome)
                .containsExactly("Maria Nova");
    }

    private static Cliente cliente(String nome, String cpf) {
        return Cliente.create(
                nome, cpf, "cliente@example.com", "11999999999",
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000");
    }
}
