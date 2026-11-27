package com.fiap.mekano.registry.rest.exception;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

/**
 * Garante que falhas de Bean Validation no corpo da requisicao tambem sejam
 * devolvidas no formato RFC 7807, em vez do formato padrao do Quarkus.
 */
@Provider
@ApplicationScoped
@Priority(1)
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyReactiveViolationException> {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ResteasyReactiveViolationException exception) {
        String detail = exception.getConstraintViolations() == null
                ? null
                : exception.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .sorted()
                        .collect(Collectors.joining("; "));

        if (detail == null || detail.isBlank()) {
            detail = "Requisicao invalida";
        }

        String instance = uriInfo != null && uriInfo.getRequestUri() != null
                ? uriInfo.getRequestUri().toString()
                : null;

        return Response.status(400)
                .entity(ProblemDetail.of(400, detail, instance))
                .type(PROBLEM_JSON)
                .build();
    }
}
