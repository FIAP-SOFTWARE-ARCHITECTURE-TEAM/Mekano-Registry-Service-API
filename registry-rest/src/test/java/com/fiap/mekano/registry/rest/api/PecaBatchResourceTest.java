package com.fiap.mekano.registry.rest.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class PecaBatchResourceTest extends AbstractResourceTest {

    private static final String PECAS = "/api/v4/pecas";
    private static final String BATCH = "/api/v4/internal/pecas/batch";

    @Test
    void deveRetornarPecasAtivasInativasENaoEncontradasNaOrdemSolicitada() {
        String ativa = criarPeca("PEA-001", "Filtro", "45.90");
        String inativa = criarPeca("PEA-002", "Pastilha", "120.00");
        given().when().delete(PECAS + "/" + inativa).then().statusCode(204);
        String inexistente = "99999999-9999-9999-9999-999999999999";

        given().contentType("application/json")
                .body("""
                        {
                          "pecaUuids": ["%s", "%s", "%s", "%s"]
                        }
                        """.formatted(ativa, ativa, inexistente, inativa))
                .when().post(BATCH)
                .then().statusCode(200)
                .body("pecas", hasSize(2))
                .body("pecas[0].uuid", is(ativa))
                .body("pecas[0].isActive", is(true))
                .body("pecas[1].uuid", is(inativa))
                .body("pecas[1].isActive", is(false))
                .body("naoEncontradas", contains(inexistente));
    }

    @Test
    void deveValidarLoteVazioEAcimaDeCem() {
        given().contentType("application/json").body("{\"pecaUuids\":[]}")
                .when().post(BATCH).then().statusCode(400);
        given().contentType("application/json").body("{\"pecaUuids\":[null]}")
                .when().post(BATCH).then().statusCode(400);

        String uuids = IntStream.range(0, 101)
                .mapToObj(index -> "\"00000000-0000-0000-0000-" + String.format("%012d", index) + "\"")
                .collect(Collectors.joining(","));
        given().contentType("application/json").body("{\"pecaUuids\":[" + uuids + "]}")
                .when().post(BATCH).then().statusCode(400);
    }

    private static String criarPeca(String codigo, String descricao, String valor) {
        return given().contentType("application/json")
                .body("""
                        {
                          "codigo": "%s",
                          "descricao": "%s",
                          "valorUnitario": %s
                        }
                        """.formatted(codigo, descricao, valor))
                .when().post(PECAS).then().statusCode(201).extract().path("uuid");
    }
}
