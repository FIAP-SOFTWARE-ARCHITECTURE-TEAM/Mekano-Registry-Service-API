package com.fiap.mekano.registry.rest.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class ServicoResourceTest extends AbstractResourceTest {

    private static final String BASE = "/api/v4/servicos";

    @Test
    void deveCriarAtualizarListarEDesativarServico() {
        String uuid = given().contentType("application/json").body(servicoJson("Troca de oleo", "150.00"))
                .when().post(BASE).then().statusCode(201)
                .header("Location", containsString(BASE + "/"))
                .body("uuid", notNullValue())
                .body("nome", is("Troca de oleo"))
                .body("isActive", is(true))
                .extract().path("uuid");

        given().contentType("application/json")
                .body("""
                        {
                          "nome": "Alinhamento",
                          "descricao": null,
                          "valorUnitario": 200.00
                        }
                        """)
                .when().put(BASE + "/" + uuid).then().statusCode(200)
                .body("nome", is("Alinhamento"))
                .body("descricao", is((Object) null));

        given().queryParam("nome", "alinha").queryParam("isActive", true)
                .when().get(BASE).then().statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].uuid", is(uuid));

        given().when().delete(BASE + "/" + uuid).then().statusCode(204);
        given().when().get(BASE + "/" + uuid).then().statusCode(200).body("isActive", is(false));
    }

    @Test
    void deveImpedirNomeDuplicadoEReativacaoEmConflito() {
        String uuid = given().contentType("application/json").body(servicoJson("Troca de oleo", "150.00"))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");
        given().contentType("application/json").body(servicoJson("Troca de oleo", "170.00"))
                .when().post(BASE).then().statusCode(409);

        given().when().delete(BASE + "/" + uuid).then().statusCode(204);
        given().contentType("application/json").body(servicoJson("Troca de oleo", "170.00"))
                .when().post(BASE).then().statusCode(201);
        given().when().patch(BASE + "/" + uuid + "/reativar")
                .then().statusCode(409).body("status", is(409));
    }

    private static String servicoJson(String nome, String valor) {
        return """
                {
                  "nome": "%s",
                  "descricao": "Servico de oficina",
                  "valorUnitario": %s
                }
                """.formatted(nome, valor);
    }
}
