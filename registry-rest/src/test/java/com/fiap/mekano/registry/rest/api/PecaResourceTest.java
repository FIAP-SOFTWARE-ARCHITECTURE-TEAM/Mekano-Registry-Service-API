package com.fiap.mekano.registry.rest.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
class PecaResourceTest extends AbstractResourceTest {

    private static final String BASE = "/api/v4/pecas";

    @Test
    void deveCriarBuscarAtualizarEReativarPecaDoCatalogo() {
        String uuid = given().contentType("application/json").body(pecaJson("PEA-001", "Filtro de oleo", "45.90"))
                .when().post(BASE)
                .then().statusCode(201)
                .header("Location", containsString(BASE + "/"))
                .body("uuid", notNullValue())
                .body("codigo", is("PEA-001"))
                .body("valorUnitario", is(45.90f))
                .body("isActive", is(true))
                .extract().path("uuid");

        given().when().get(BASE + "/" + uuid).then()
                .statusCode(200)
                .body("descricao", is("Filtro de oleo"))
                .body("$", not(hasKey("saldoAtual")))
                .body("$", not(hasKey("saldoReservado")))
                .body("$", not(hasKey("estoqueMinimo")));

        given().contentType("application/json")
                .body(pecaJson("PEA-001", "Filtro premium", "55.00"))
                .when().put(BASE + "/" + uuid)
                .then().statusCode(200)
                .body("descricao", is("Filtro premium"))
                .body("valorUnitario", is(55.0f));

        given().when().delete(BASE + "/" + uuid).then().statusCode(204);
        given().when().patch(BASE + "/" + uuid + "/reativar").then().statusCode(204);
        given().when().get(BASE + "/" + uuid).then().statusCode(200).body("isActive", is(true));
    }

    @Test
    void deveRejeitarCodigoDuplicadoEValorInvalido() {
        given().contentType("application/json").body(pecaJson("PEA-001", "Filtro", "45.90"))
                .when().post(BASE).then().statusCode(201);
        given().contentType("application/json").body(pecaJson("PEA-001", "Outro filtro", "30.00"))
                .when().post(BASE).then().statusCode(409).body("status", is(409));
        given().contentType("application/json").body(pecaJson("PEA-003", "Filtro", "0"))
                .when().post(BASE).then().statusCode(400).body("status", is(400));
    }

    @Test
    void deveListarComFiltrosPorCodigoDescricaoEStatus() {
        String primeiro = given().contentType("application/json")
                .body(pecaJson("PEA-001", "Filtro de oleo", "45.90"))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");
        given().contentType("application/json")
                .body(pecaJson("PEA-002", "Pastilha de freio", "120.00"))
                .when().post(BASE).then().statusCode(201);
        given().when().delete(BASE + "/" + primeiro).then().statusCode(204);

        given().queryParam("codigo", "pea-002").queryParam("descricao", "freio").queryParam("isActive", true)
                .when().get(BASE).then().statusCode(200).body("content", hasSize(1))
                .body("content[0].codigo", is("PEA-002"))
                .body("totalElements", is(1));
        given().queryParam("isActive", false).when().get(BASE)
                .then().statusCode(200).body("content", hasSize(1));
    }

    private static String pecaJson(String codigo, String descricao, String valor) {
        return """
                {
                  "codigo": "%s",
                  "descricao": "%s",
                  "valorUnitario": %s
                }
                """.formatted(codigo, descricao, valor);
    }
}
