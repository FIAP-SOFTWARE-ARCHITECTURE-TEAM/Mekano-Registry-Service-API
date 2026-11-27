package com.fiap.mekano.registry.rest.api;

import com.fiap.mekano.registry.domain.model.Servico;
import com.fiap.mekano.registry.domain.port.in.ServicoServicePort;
import com.fiap.mekano.registry.rest.dto.CreateServicoRequest;
import com.fiap.mekano.registry.rest.dto.ServicoPageResponse;
import com.fiap.mekano.registry.rest.dto.ServicoResponse;
import com.fiap.mekano.registry.rest.dto.UpdateServicoRequest;
import com.fiap.mekano.registry.rest.mapper.ServicoDtoMapper;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/servicos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Servicos", description = "Gerenciamento do catalogo de servicos")
public class ServicoResource {

    private final ServicoServicePort service;
    private final ServicoDtoMapper mapper;

    @Inject
    public ServicoResource(ServicoServicePort service, ServicoDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cadastrar servico")
    public Response create(@Valid CreateServicoRequest request, @Context UriInfo uriInfo) {
        Servico servico = service.create(mapper.toCommand(request));
        ServicoResponse response = mapper.toResponse(servico);
        URI location = uriInfo.getAbsolutePathBuilder().path(response.uuid().toString()).build();
        return Response.created(location).entity(response).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @GET
    @Operation(summary = "Listar servicos")
    public Response listAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("nome,asc") String sort,
            @QueryParam("nome") String nome,
            @QueryParam("isActive") Boolean isActive) {
        int normalizedPage = Pagination.page(page);
        int normalizedSize = Pagination.size(size);
        List<ServicoResponse> content = service
                .findAll(normalizedPage, normalizedSize, sort, nome, isActive)
                .stream()
                .map(mapper::toResponse)
                .toList();
        long total = service.countAll(nome, isActive);
        return Response.ok(new ServicoPageResponse(
                content, normalizedPage, normalizedSize, total, Pagination.totalPages(total, normalizedSize))).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @GET
    @Path("/{uuid}")
    @Operation(summary = "Buscar servico por UUID")
    public Response findById(@PathParam("uuid") UUID uuid) {
        return Response.ok(mapper.toResponse(service.findById(uuid))).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @PUT
    @Path("/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualizar servico")
    public Response update(@PathParam("uuid") UUID uuid, @Valid UpdateServicoRequest request) {
        return Response.ok(mapper.toResponse(service.update(uuid, mapper.toCommand(request)))).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Desativar servico")
    public Response delete(@PathParam("uuid") UUID uuid) {
        service.delete(uuid);
        return Response.noContent().build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @PATCH
    @Path("/{uuid}/reativar")
    @Operation(summary = "Reativar servico")
    public Response reactivate(@PathParam("uuid") UUID uuid) {
        service.reactivate(uuid);
        return Response.noContent().build();
    }
}
