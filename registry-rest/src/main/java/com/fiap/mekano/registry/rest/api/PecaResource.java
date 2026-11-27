package com.fiap.mekano.registry.rest.api;

import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.domain.port.in.PecaServicePort;
import com.fiap.mekano.registry.rest.dto.CreatePecaRequest;
import com.fiap.mekano.registry.rest.dto.PecaPageResponse;
import com.fiap.mekano.registry.rest.dto.PecaResponse;
import com.fiap.mekano.registry.rest.dto.UpdatePecaRequest;
import com.fiap.mekano.registry.rest.exception.ProblemDetail;
import com.fiap.mekano.registry.rest.mapper.PecaDtoMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/pecas")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Pecas", description = "Catalogo de pecas, sem controle de estoque")
public class PecaResource {

    private final PecaServicePort service;
    private final PecaDtoMapper mapper;

    @Inject
    public PecaResource(PecaServicePort service, PecaDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cadastrar peca no catalogo")
    @APIResponse(responseCode = "201", description = "Peca criada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PecaResponse.class)))
    @APIResponse(responseCode = "400", description = "Dados invalidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @APIResponse(responseCode = "409", description = "Codigo ja cadastrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public Response create(@Valid CreatePecaRequest request, @Context UriInfo uriInfo) {
        Peca peca = service.create(mapper.toCommand(request));
        PecaResponse response = mapper.toResponse(peca);
        URI location = uriInfo.getAbsolutePathBuilder().path(response.uuid().toString()).build();
        return Response.created(location).entity(response).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @GET
    @Operation(summary = "Listar pecas do catalogo")
    @APIResponse(responseCode = "200", description = "Lista paginada de pecas",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PecaPageResponse.class)))
    public Response listAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("codigo,asc") String sort,
            @QueryParam("codigo") String codigo,
            @QueryParam("descricao") String descricao,
            @QueryParam("isActive") Boolean isActive) {
        int normalizedPage = Pagination.page(page);
        int normalizedSize = Pagination.size(size);
        List<PecaResponse> content = service
                .findAll(normalizedPage, normalizedSize, sort, codigo, descricao, isActive)
                .stream()
                .map(mapper::toResponse)
                .toList();
        long total = service.countAll(codigo, descricao, isActive);
        return Response.ok(new PecaPageResponse(
                content, normalizedPage, normalizedSize, total, Pagination.totalPages(total, normalizedSize))).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @GET
    @Path("/{uuid}")
    @Operation(summary = "Buscar peca por UUID")
    @APIResponse(responseCode = "200", description = "Peca encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PecaResponse.class)))
    @APIResponse(responseCode = "404", description = "Peca nao encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public Response findById(@PathParam("uuid") UUID uuid) {
        return Response.ok(mapper.toResponse(service.findById(uuid))).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @PUT
    @Path("/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualizar peca do catalogo")
    @APIResponse(responseCode = "200", description = "Peca atualizada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PecaResponse.class)))
    @APIResponse(responseCode = "400", description = "Dados invalidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @APIResponse(responseCode = "404", description = "Peca nao encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @APIResponse(responseCode = "409", description = "Codigo ja cadastrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public Response update(@PathParam("uuid") UUID uuid, @Valid UpdatePecaRequest request) {
        return Response.ok(mapper.toResponse(service.update(uuid, mapper.toCommand(request)))).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Desativar peca")
    @APIResponse(responseCode = "204", description = "Peca desativada")
    @APIResponse(responseCode = "404", description = "Peca nao encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public Response delete(@PathParam("uuid") UUID uuid) {
        service.delete(uuid);
        return Response.noContent().build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @PATCH
    @Path("/{uuid}/reativar")
    @Operation(summary = "Reativar peca")
    @APIResponse(responseCode = "204", description = "Peca reativada")
    @APIResponse(responseCode = "404", description = "Peca nao encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @APIResponse(responseCode = "409", description = "Codigo em uso por outra peca ativa",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public Response reactivate(@PathParam("uuid") UUID uuid) {
        service.reactivate(uuid);
        return Response.noContent().build();
    }
}
