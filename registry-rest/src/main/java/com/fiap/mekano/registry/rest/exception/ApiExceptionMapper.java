package com.fiap.mekano.registry.rest.exception;

import com.fiap.mekano.registry.domain.exception.AppException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

/**
 * Converte excecoes da aplicacao em respostas RFC 7807
 * ({@code application/problem+json}).
 */
@Provider
@ApplicationScoped
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof AppException ex) {
            return build(ex.getStatus(), ex.getMessage());
        }
        if (exception instanceof ConstraintViolationException ex) {
            return build(400, violationsDetail(ex));
        }
        if (exception instanceof ValidationException ex) {
            return build(400, ex.getMessage());
        }
        if (exception instanceof WebApplicationException ex) {
            return build(webStatus(ex), webDetail(ex));
        }

        Log.errorf(exception, "Excecao nao tratada: %s", exception.getMessage());
        return build(500, "Erro interno do servidor");
    }

    private static int webStatus(WebApplicationException exception) {
        return exception.getResponse() != null ? exception.getResponse().getStatus() : 500;
    }

    private static String webDetail(WebApplicationException exception) {
        String detail = exception.getMessage();
        if (detail != null && !detail.isBlank()) {
            return detail;
        }
        Response response = exception.getResponse();
        if (response != null && response.getStatusInfo() != null) {
            return response.getStatusInfo().getReasonPhrase();
        }
        return "Erro interno do servidor";
    }

    private static String violationsDetail(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations() == null
                ? null
                : exception.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .sorted()
                        .collect(Collectors.joining("; "));
        return detail == null || detail.isBlank() ? "Requisicao invalida" : detail;
    }

    private Response build(int status, String detail) {
        String instance = uriInfo != null && uriInfo.getRequestUri() != null
                ? uriInfo.getRequestUri().toString()
                : null;
        return Response.status(status)
                .entity(ProblemDetail.of(status, detail, instance))
                .type(PROBLEM_JSON)
                .build();
    }
}
