package com.fiap.mekano.registry.rest.api;

import com.fiap.mekano.registry.infrastructure.repository.ClientePanacheRepository;
import com.fiap.mekano.registry.infrastructure.repository.PecaPanacheRepository;
import com.fiap.mekano.registry.infrastructure.repository.ServicoPanacheRepository;
import com.fiap.mekano.registry.infrastructure.repository.VeiculoPanacheRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base para testes REST: limpa as tabelas antes de cada teste.
 */
abstract class AbstractResourceTest {

    @Inject
    ClientePanacheRepository clientePanacheRepository;

    @Inject
    VeiculoPanacheRepository veiculoPanacheRepository;

    @Inject
    PecaPanacheRepository pecaPanacheRepository;

    @Inject
    ServicoPanacheRepository servicoPanacheRepository;

    @BeforeEach
    void limparBanco() {
        QuarkusTransaction.requiringNew().run(() -> {
            pecaPanacheRepository.deleteAll();
            servicoPanacheRepository.deleteAll();
            veiculoPanacheRepository.deleteAll();
            clientePanacheRepository.deleteAll();
        });
    }
}
