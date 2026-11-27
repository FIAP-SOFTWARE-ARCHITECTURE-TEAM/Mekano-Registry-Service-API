package com.fiap.mekano.registry.rest.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ObservabilityTest {

    @Test
    void healthDeveEstarUp() {
        given()
                .when().get("/q/health")
                .then()
                .statusCode(200)
                .body("status", is("UP"));
    }

    @Test
    void metricasPrometheusDevemEstarDisponiveis() {
        given()
                .when().get("/q/metrics")
                .then()
                .statusCode(200);
    }

    @Test
    void openapiDeveEstarDisponivel() {
        String spec = given()
                .when().get("/q/openapi")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertThat(spec)
                .contains(
                        "Mekano Registry Service API",
                        "/api/v4/clientes",
                        "/api/v4/veiculos",
                        "/api/v4/pecas",
                        "/api/v4/servicos",
                        "/api/v4/internal/pecas/batch",
                        "ClienteResponse",
                        "PecaBatchRequest",
                        "PecaBatchResponse",
                        "ProblemDetail");
    }
}
