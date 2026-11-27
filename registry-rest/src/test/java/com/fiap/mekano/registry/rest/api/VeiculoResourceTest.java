package com.fiap.mekano.registry.rest.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class VeiculoResourceTest extends AbstractResourceTest {

    private static final String CLIENTES = "/api/v4/clientes";
    private static final String BASE = "/api/v4/veiculos";
    private static final String CPF_MARIA = "52998224725";
    private static final String CPF_JOAO = "11144477735";

    @Test
    void deveCriarEBuscarVeiculo() {
        String clienteUuid = criarCliente("Maria", CPF_MARIA);

        String uuid = given()
                .contentType("application/json")
                .body(veiculoJson(clienteUuid, "abc1d23"))
                .when().post(BASE)
                .then()
                .statusCode(201)
                .header("Location", containsString(BASE + "/"))
                .body("uuid", notNullValue())
                .body("clienteUuid", is(clienteUuid))
                .body("placa", is("ABC1D23"))
                .body("marca", is("Volkswagen"))
                .body("modelo", is("Gol"))
                .body("ano", is(2020))
                .body("isActive", is(true))
                .extract().path("uuid");

        given().when().get(BASE + "/" + uuid)
                .then().statusCode(200)
                .body("uuid", is(uuid))
                .body("placa", is("ABC1D23"));
    }

    @Test
    void deveRejeitarPlacaDuplicada() {
        String clienteUuid = criarCliente("Maria", CPF_MARIA);

        given().contentType("application/json").body(veiculoJson(clienteUuid, "ABC1D23"))
                .when().post(BASE).then().statusCode(201);

        given()
                .contentType("application/json")
                .body(veiculoJson(clienteUuid, "ABC-1D23"))
                .when().post(BASE)
                .then()
                .statusCode(409)
                .contentType(containsString("application/problem+json"))
                .body("status", is(409));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExiste() {
        given()
                .contentType("application/json")
                .body(veiculoJson("00000000-0000-0000-0000-000000000000", "ABC1D23"))
                .when().post(BASE)
                .then()
                .statusCode(404)
                .contentType(containsString("application/problem+json"))
                .body("status", is(404));
    }

    @Test
    void deveRetornar409QuandoClienteInativo() {
        String clienteUuid = criarCliente("Maria", CPF_MARIA);
        given().when().delete(CLIENTES + "/" + clienteUuid).then().statusCode(204);

        given()
                .contentType("application/json")
                .body(veiculoJson(clienteUuid, "ABC1D23"))
                .when().post(BASE)
                .then()
                .statusCode(409)
                .contentType(containsString("application/problem+json"))
                .body("status", is(409));
    }

    @Test
    void deveRejeitarPlacaInvalida() {
        String clienteUuid = criarCliente("Maria", CPF_MARIA);

        given()
                .contentType("application/json")
                .body(veiculoJson(clienteUuid, "AB12345"))
                .when().post(BASE)
                .then()
                .statusCode(400)
                .contentType(containsString("application/problem+json"))
                .body("status", is(400));
    }

    @Test
    void deveListarComFiltros() {
        String mariaUuid = criarCliente("Maria", CPF_MARIA);
        String joaoUuid = criarCliente("Joao", CPF_JOAO);

        given().contentType("application/json").body(veiculoJson(mariaUuid, "ABC1D23"))
                .when().post(BASE).then().statusCode(201);
        given().contentType("application/json").body(veiculoJson(mariaUuid, "XYZ9876"))
                .when().post(BASE).then().statusCode(201);
        given().contentType("application/json").body(veiculoJson(joaoUuid, "DEF5678"))
                .when().post(BASE).then().statusCode(201);

        given()
                .queryParam("clienteUuid", mariaUuid)
                .queryParam("isActive", true)
                .when().get(BASE)
                .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("totalElements", is(2));

        given()
                .queryParam("placa", "abc")
                .when().get(BASE)
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].placa", is("ABC1D23"))
                .body("totalElements", is(1));
    }

    @Test
    void deveAtualizarVeiculoSemAlterarPlaca() {
        String clienteUuid = criarCliente("Maria", CPF_MARIA);
        String uuid = given().contentType("application/json").body(veiculoJson(clienteUuid, "ABC1D23"))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");

        given()
                .contentType("application/json")
                .body("""
                        {
                          "marca": "Fiat",
                          "modelo": "Uno",
                          "ano": 2015
                        }
                        """)
                .when().put(BASE + "/" + uuid)
                .then()
                .statusCode(200)
                .body("placa", is("ABC1D23"))
                .body("clienteUuid", is(clienteUuid))
                .body("marca", is("Fiat"))
                .body("modelo", is("Uno"))
                .body("ano", is(2015));
    }

    @Test
    void deveDesativarEReativarVeiculo() {
        String clienteUuid = criarCliente("Maria", CPF_MARIA);
        String uuid = given().contentType("application/json").body(veiculoJson(clienteUuid, "ABC1D23"))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");

        given().when().delete(BASE + "/" + uuid).then().statusCode(204);

        given().when().get(BASE + "/" + uuid)
                .then().statusCode(200).body("isActive", is(false));

        given().when().patch(BASE + "/" + uuid + "/reativar").then().statusCode(204);

        given().when().get(BASE + "/" + uuid)
                .then().statusCode(200).body("isActive", is(true));
    }

    @Test
    void naoDeveReativarVeiculoQuandoPlacaFoiReutilizada() {
        String clienteUuid = criarCliente("Maria", CPF_MARIA);
        String uuidAntigo = given().contentType("application/json").body(veiculoJson(clienteUuid, "ABC1D23"))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");
        given().when().delete(BASE + "/" + uuidAntigo).then().statusCode(204);

        given().contentType("application/json").body(veiculoJson(clienteUuid, "ABC1D23"))
                .when().post(BASE).then().statusCode(201);

        given().when().patch(BASE + "/" + uuidAntigo + "/reativar")
                .then()
                .statusCode(409)
                .contentType(containsString("application/problem+json"))
                .body("status", is(409));
    }

    @Test
    void deveRetornar404ParaVeiculoInexistente() {
        given().when().get(BASE + "/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404)
                .contentType(containsString("application/problem+json"))
                .body("status", is(404));
    }

    private String criarCliente(String nome, String cpf) {
        return given()
                .contentType("application/json")
                .body("""
                        {
                          "nome": "%s",
                          "cpf": "%s",
                          "email": "cliente@example.com",
                          "telefone": "11999999999",
                          "endereco": {
                            "logradouro": "Rua Exemplo",
                            "numero": "100",
                            "bairro": "Centro",
                            "cidade": "Sao Paulo",
                            "uf": "SP",
                            "cep": "01001000"
                          }
                        }
                        """.formatted(nome, cpf))
                .when().post(CLIENTES)
                .then().statusCode(201)
                .extract().path("uuid");
    }

    private static String veiculoJson(String clienteUuid, String placa) {
        return """
                {
                  "clienteUuid": "%s",
                  "placa": "%s",
                  "marca": "Volkswagen",
                  "modelo": "Gol",
                  "ano": 2020
                }
                """.formatted(clienteUuid, placa);
    }
}
