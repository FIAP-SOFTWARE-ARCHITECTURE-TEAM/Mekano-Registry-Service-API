package com.fiap.mekano.registry.rest.exception;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Resposta de erro no formato RFC 7807 - Problem Details for HTTP APIs.
 *
 * <pre>{@code
 * {
 *   "type": "about:blank",
 *   "title": "Recurso nao encontrado",
 *   "status": 404,
 *   "detail": "Cliente nao encontrado: ...",
 *   "instance": "/api/v4/clientes/..."
 * }
 * }</pre>
 */
@Schema(name = "ProblemDetail", description = "Resposta de erro no formato RFC 7807 Problem Details")
public record ProblemDetail(
        @Schema(description = "URI que identifica o tipo do problema (RFC 7807)", example = "about:blank") String type,
        @Schema(description = "Resumo legivel do problema", example = "Recurso nao encontrado") String title,
        @Schema(description = "Codigo de status HTTP", example = "404") int status,
        @Schema(description = "Explicacao especifica desta ocorrencia", example = "Cliente nao encontrado") String detail,
        @Schema(description = "URI que identifica a ocorrencia especifica", example = "/api/v4/clientes/123") String instance) {

    public static ProblemDetail of(int status, String detail) {
        return new ProblemDetail("about:blank", titleFor(status), status, detail, null);
    }

    public static ProblemDetail of(int status, String detail, String instance) {
        return new ProblemDetail("about:blank", titleFor(status), status, detail, instance);
    }

    private static String titleFor(int status) {
        return switch (status) {
            case 400 -> "Requisicao invalida";
            case 401 -> "Nao autenticado";
            case 403 -> "Acesso negado";
            case 404 -> "Recurso nao encontrado";
            case 405 -> "Metodo nao permitido";
            case 409 -> "Conflito";
            case 415 -> "Tipo de midia nao suportado";
            case 422 -> "Entidade nao processavel";
            case 429 -> "Muitas requisicoes";
            case 500 -> "Erro interno do servidor";
            default -> "Erro";
        };
    }
}
