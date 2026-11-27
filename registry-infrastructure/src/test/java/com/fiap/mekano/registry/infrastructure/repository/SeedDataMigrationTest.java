package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import com.fiap.mekano.registry.domain.port.out.PecaRepositoryPort;
import com.fiap.mekano.registry.domain.port.out.ServicoRepositoryPort;
import com.fiap.mekano.registry.domain.port.out.VeiculoRepositoryPort;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class SeedDataMigrationTest {

    private static final UUID CLIENTE_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VEICULO_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SERVICO_1_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SERVICO_2_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PECA_1_UUID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID PECA_2_UUID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Inject
    EntityManager entityManager;

    @Inject
    ClienteRepositoryPort clienteRepository;

    @Inject
    VeiculoRepositoryPort veiculoRepository;

    @Inject
    ServicoRepositoryPort servicoRepository;

    @Inject
    PecaRepositoryPort pecaRepository;

    @Test
    void deveAplicarSeedEPermitirReexecucaoSemDuplicarRegistros() throws IOException {
        applySeedMigration();
        applySeedMigration();

        assertThat(clienteRepository.findById(CLIENTE_UUID))
                .isPresent()
                .get()
                .satisfies(cliente -> {
                    assertThat(cliente.getNome()).isEqualTo("Cliente Exemplo");
                    assertThat(cliente.getCpf().getValue()).isEqualTo("52998224725");
                });
        assertThat(veiculoRepository.findById(VEICULO_UUID))
                .isPresent()
                .get()
                .satisfies(veiculo -> {
                    assertThat(veiculo.getClienteUuid()).isEqualTo(CLIENTE_UUID);
                    assertThat(veiculo.getPlaca().getValue()).isEqualTo("ABC1D23");
                });
        assertThat(servicoRepository.findById(SERVICO_1_UUID)).isPresent();
        assertThat(servicoRepository.findById(SERVICO_2_UUID)).isPresent();
        assertThat(pecaRepository.findById(PECA_1_UUID)).isPresent();
        assertThat(pecaRepository.findById(PECA_2_UUID)).isPresent();
        assertThat(entityManager.createQuery("select count(c) from ClienteEntity c", Long.class).getSingleResult())
                .isEqualTo(1L);
        assertThat(entityManager.createQuery("select count(v) from VeiculoEntity v", Long.class).getSingleResult())
                .isEqualTo(1L);
        assertThat(entityManager.createQuery("select count(s) from ServicoEntity s", Long.class).getSingleResult())
                .isEqualTo(2L);
        assertThat(entityManager.createQuery("select count(p) from PecaEntity p", Long.class).getSingleResult())
                .isEqualTo(2L);
    }

    private void applySeedMigration() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/db/migration/V5__seed_dados_iniciais.sql")) {
            if (input == null) {
                throw new IllegalStateException("Migration V5__seed_dados_iniciais.sql nao encontrada no classpath");
            }
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String statement : script.split(";")) {
                if (!statement.isBlank()) {
                    entityManager.createNativeQuery(statement).executeUpdate();
                }
            }
        }
    }
}
