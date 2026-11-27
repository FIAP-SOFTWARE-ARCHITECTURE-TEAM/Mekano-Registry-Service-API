package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.model.Servico;
import com.fiap.mekano.registry.domain.port.out.ServicoRepositoryPort;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class ServicoRepositoryImplTest {

    @Inject
    ServicoRepositoryPort repository;

    @Test
    void devePersistirFiltrarAtualizarEExcluirServico() {
        Servico criado = repository.create(Servico.create(
                "Troca de oleo", "Troca de oleo e filtro", new BigDecimal("150.00")));
        Servico atualizado = Servico.reconstitute(
                criado.getId(), "Alinhamento", null, new BigDecimal("200.00"),
                criado.getCreatedAt(), criado.getIsActive());

        assertThat(repository.update(atualizado).getNome()).isEqualTo("Alinhamento");
        assertThat(repository.findAll(0, 20, "nome,asc", "alinha", true))
                .extracting(Servico::getId)
                .containsExactly(criado.getId());

        repository.markAsDeleted(criado.getId());
        assertThat(repository.countAll(null, true)).isZero();
        assertThat(repository.countAll(null, false)).isEqualTo(1);
    }
}
