package com.fiap.mekano.registry.rest.api;

import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.domain.port.in.PecaServicePort;
import com.fiap.mekano.registry.rest.dto.PecaBatchItemResponse;
import com.fiap.mekano.registry.rest.dto.PecaBatchRequest;
import com.fiap.mekano.registry.rest.dto.PecaBatchResponse;
import com.fiap.mekano.registry.rest.exception.ProblemDetail;
import com.fiap.mekano.registry.rest.mapper.PecaDtoMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/internal/pecas")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Pecas internas", description = "Consultas internas para integracao entre servicos")
public class PecaBatchResource {

    private final PecaServicePort service;
    private final PecaDtoMapper mapper;

    @Inject
    public PecaBatchResource(PecaServicePort service, PecaDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // TODO: habilitar quando autenticacao entre servicos entrar no escopo
    // @RolesAllowed("service:supplies-purchase")
    @POST
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Consultar pecas por UUID em lote")
    @APIResponse(responseCode = "200", description = "Pecas encontradas e UUIDs inexistentes",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PecaBatchResponse.class)))
    @APIResponse(responseCode = "400", description = "Lote invalido",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public Response findBatch(@Valid PecaBatchRequest request) {
        List<UUID> requestedIds = new ArrayList<>(new LinkedHashSet<>(request.pecaUuids()));
        List<Peca> found = service.findByIds(requestedIds);
        Map<UUID, Peca> byUuid = new HashMap<>();
        found.forEach(peca -> byUuid.put(peca.getId(), peca));

        List<PecaBatchItemResponse> pecas = requestedIds.stream()
                .map(byUuid::get)
                .filter(peca -> peca != null)
                .map(mapper::toBatchItem)
                .toList();
        List<UUID> naoEncontradas = requestedIds.stream()
                .filter(uuid -> !byUuid.containsKey(uuid))
                .toList();

        return Response.ok(new PecaBatchResponse(pecas, naoEncontradas)).build();
    }
}
