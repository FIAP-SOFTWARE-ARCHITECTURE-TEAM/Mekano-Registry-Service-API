package com.fiap.mekano.registry.rest.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class ClienteResourceTest extends AbstractResourceTest {

    private static final String BASE = "/api/v4/clientes";
    private static final String CPF_MARIA = "52998224725";
    private static final String CPF_JOAO = "11144477735";

    @Test
    void deveCriarEBuscarCliente() {
        String uuid = given()
                .contentType("application/json")
                .body(clienteJson("Maria Silva", CPF_MARIA))
                .when().post(BASE)
                .then()
                .statusCode(201)
                .header("Location", containsString(BASE + "/"))
                .body("uuid", notNullValue())
                .body("nome", is("Maria Silva"))
                .body("cpf", is(CPF_MARIA))
                .body("email", is("cliente@example.com"))
                .body("telefone", is("11999999999"))
                .body("endereco.uf", is("SP"))
                .body("endereco.cep", is("01001000"))
                .body("isActive", is(true))
                .body("createdAt", notNullValue())
                .extract().path("uuid");

        given()
                .when().get(BASE + "/" + uuid)
                .then()
                .statusCode(200)
                .body("uuid", is(uuid))
                .body("nome", is("Maria Silva"));
    }

    @Test
    void deveRejeitarCpfInvalido() {
        given()
                .contentType("application/json")
                .body(clienteJson("Maria Silva", "12345678900"))
                .when().post(BASE)
                .then()
                .statusCode(400)
                .contentType(containsString("application/problem+json"))
                .body("status", is(400))
                .body("title", is("Requisicao invalida"))
                .body("instance", containsString(BASE));
    }

    @Test
    void deveRejeitarCpfDuplicado() {
        given().contentType("application/json").body(clienteJson("Maria", CPF_MARIA))
                .when().post(BASE).then().statusCode(201);

        given()
                .contentType("application/json")
                .body(clienteJson("Outra Maria", CPF_MARIA))
                .when().post(BASE)
                .then()
                .statusCode(409)
                .contentType(containsString("application/problem+json"))
                .body("status", is(409));
    }

    @Test
    void deveRejeitarPayloadInvalido() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "nome": "",
                          "cpf": "52998224725",
                          "email": "email-invalido",
                          "endereco": {
                            "logradouro": "Rua Exemplo",
                            "cidade": "Sao Paulo",
                            "uf": "SP",
                            "cep": "01001000"
                          }
                        }
                        """)
                .when().post(BASE)
                .then()
                .statusCode(400)
                .contentType(containsString("application/problem+json"))
                .body("status", is(400));
    }

    @Test
    void deveListarComFiltrosEPaginacao() {
        given().contentType("application/json").body(clienteJson("Maria Silva", CPF_MARIA))
                .when().post(BASE).then().statusCode(201);
        given().contentType("application/json").body(clienteJson("Joao Souza", CPF_JOAO))
                .when().post(BASE).then().statusCode(201);

        given()
                .queryParam("page", 0)
                .queryParam("size", 20)
                .queryParam("nome", "maria")
                .queryParam("isActive", true)
                .when().get(BASE)
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].nome", is("Maria Silva"))
                .body("page", is(0))
                .body("size", is(20))
                .body("totalElements", is(1))
                .body("totalPages", is(1));

        given()
                .queryParam("size", 1000)
                .when().get(BASE)
                .then()
                .statusCode(200)
                .body("size", is(100))
                .body("totalElements", is(2));
    }

    @Test
    void deveAtualizarClienteSemAlterarCpf() {
        String uuid = given().contentType("application/json").body(clienteJson("Maria Silva", CPF_MARIA))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");

        given()
                .contentType("application/json")
                .body("""
                        {
                          "nome": "Maria Atualizada",
                          "email": "maria.nova@example.com",
                          "telefone": "11988887777",
                          "endereco": {
                            "logradouro": "Rua Nova",
                            "numero": "20",
                            "complemento": "Apto 1",
                            "bairro": "Centro",
                            "cidade": "Campinas",
                            "uf": "SP",
                            "cep": "13010000"
                          }
                        }
                        """)
                .when().put(BASE + "/" + uuid)
                .then()
                .statusCode(200)
                .body("nome", is("Maria Atualizada"))
                .body("email", is("maria.nova@example.com"))
                .body("cpf", is(CPF_MARIA))
                .body("endereco.cidade", is("Campinas"));
    }

    @Test
    void deveDesativarEReativarCliente() {
        String uuid = given().contentType("application/json").body(clienteJson("Maria", CPF_MARIA))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");

        given().when().delete(BASE + "/" + uuid).then().statusCode(204);

        given().when().get(BASE + "/" + uuid)
                .then().statusCode(200).body("isActive", is(false));

        given().when().patch(BASE + "/" + uuid + "/reativar").then().statusCode(204);

        given().when().get(BASE + "/" + uuid)
                .then().statusCode(200).body("isActive", is(true));
    }

    @Test
    void naoDeveReativarClienteQuandoCpfFoiReutilizado() {
        String uuidAntigo = given().contentType("application/json").body(clienteJson("Maria Antiga", CPF_MARIA))
                .when().post(BASE).then().statusCode(201).extract().path("uuid");
        given().when().delete(BASE + "/" + uuidAntigo).then().statusCode(204);

        given().contentType("application/json").body(clienteJson("Maria Nova", CPF_MARIA))
                .when().post(BASE).then().statusCode(201);

        given().when().patch(BASE + "/" + uuidAntigo + "/reativar")
                .then()
                .statusCode(409)
                .contentType(containsString("application/problem+json"))
                .body("status", is(409));
    }

    @Test
    void deveRetornar404ParaClienteInexistente() {
        given().when().get(BASE + "/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404)
                .contentType(containsString("application/problem+json"))
                .body("status", is(404));
    }

    @Test
    void deveRetornar404AoAtualizarClienteInexistente() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "nome": "Maria",
                          "email": "maria@example.com",
                          "endereco": {
                            "logradouro": "Rua A",
                            "cidade": "Sao Paulo",
                            "uf": "SP",
                            "cep": "01001000"
                          }
                        }
                        """)
                .when().put(BASE + "/00000000-0000-0000-0000-000000000000")
                .then().statusCode(404);
    }

    private static String clienteJson(String nome, String cpf) {
        return """
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
                """.formatted(nome, cpf);
    }
}
