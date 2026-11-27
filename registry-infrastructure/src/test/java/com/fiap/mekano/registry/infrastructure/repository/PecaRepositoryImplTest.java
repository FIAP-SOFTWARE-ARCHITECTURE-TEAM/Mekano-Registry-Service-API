package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.domain.port.out.PecaRepositoryPort;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class PecaRepositoryImplTest {

    @Inject
    PecaRepositoryPort repository;

    @Test
    void devePersistirConsultarEmLoteFiltrarEReativarPeca() {
        Peca salva = repository.create(Peca.create("PEA-001", "Filtro de oleo", new BigDecimal("45.90")));
        Peca outra = repository.create(Peca.create("PEA-002", "Pastilha de freio", new BigDecimal("120.00")));
        repository.markAsDeleted(outra.getId());

        assertThat(repository.findByIds(List.of(salva.getId(), outra.getId(), UUID.randomUUID())))
                .extracting(Peca::getId)
                .containsExactlyInAnyOrder(salva.getId(), outra.getId());
        assertThat(repository.findAll(0, 20, "codigo,asc", "pea-001", "filtro", true))
                .extracting(Peca::getCodigo)
                .containsExactly("PEA-001");
        assertThat(repository.countAll(null, null, false)).isEqualTo(1);

        repository.reactivate(outra.getId());
        assertThat(repository.findById(outra.getId())).get()
                .extracting(Peca::getIsActive)
                .isEqualTo(true);
    }
}
