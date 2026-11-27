package com.fiap.mekano.registry.rest.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

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
    void openapiDeveEstarDisponivel() {
        given()
                .when().get("/q/openapi")
                .then()
                .statusCode(200);
    }
}
