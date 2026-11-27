package com.fiap.mekano.registry.rest.api;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

/**
 * Configuracao JAX-RS + OpenAPI do Mekano Registry.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Mekano Registry Service API",
                version = "4.0.0",
                description = """
                        API de dados mestres e catalogo do Mekano.
                        Gerencia clientes, veiculos, catalogo de pecas e servicos.
                        O endpoint interno de batch de pecas e consumido pelo Supplies.
                        Estoque e compras pertencem ao servico Supplies.
                        """
        )
)
public class RegistryApplication extends Application {
}
