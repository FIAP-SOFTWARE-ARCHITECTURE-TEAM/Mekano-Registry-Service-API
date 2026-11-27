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
                description = "Dados mestres e catalogo do Mekano - Fase 4"
        )
)
public class RegistryApplication extends Application {
}
