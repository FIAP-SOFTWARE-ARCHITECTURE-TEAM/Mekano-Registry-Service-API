package com.fiap.mekano.registry.rest.api;

import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.port.in.ClienteServicePort;
import com.fiap.mekano.registry.domain.port.in.CreateClienteCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateClienteCommand;
import com.fiap.mekano.registry.rest.dto.ClientePageResponse;
import com.fiap.mekano.registry.rest.dto.ClienteResponse;
import com.fiap.mekano.registry.rest.dto.CreateClienteRequest;
import com.fiap.mekano.registry.rest.dto.UpdateClienteRequest;
import com.fiap.mekano.registry.rest.mapper.ClienteDtoMapper;
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

/**
 * Endpoints de clientes. Transacoes ficam na camada de aplicacao.
 */
@Path("/clientes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Clientes", description = "Gerenciamento de clientes")
public class ClienteResource {

    private final ClienteServicePort clienteService;
    private final ClienteDtoMapper clienteDtoMapper;

    @Inject
    public ClienteResource(ClienteServicePort clienteService, ClienteDtoMapper clienteDtoMapper) {
        this.clienteService = clienteService;
        this.clienteDtoMapper = clienteDtoMapper;
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed({"admin", "atendente"})
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cadastrar cliente", description = "Cadastra um novo cliente no sistema.")
    public Response create(@Valid CreateClienteRequest request, @Context UriInfo uriInfo) {
        CreateClienteCommand command = clienteDtoMapper.toCommand(request);
        Cliente cliente = clienteService.execute(command);
        ClienteResponse response = clienteDtoMapper.toResponse(cliente);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(response.uuid().toString())
                .build();

        return Response.created(location).entity(response).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed({"admin", "atendente"})
    @GET
    @Operation(summary = "Listar clientes", description = "Lista clientes com paginacao e filtros.")
    public Response listAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("nome,asc") String sort,
            @QueryParam("nome") String nome,
            @QueryParam("isActive") Boolean isActive) {

        int normalizedPage = Pagination.page(page);
        int normalizedSize = Pagination.size(size);

        List<ClienteResponse> content = clienteService
                .findAll(normalizedPage, normalizedSize, sort, nome, isActive)
                .stream()
                .map(clienteDtoMapper::toResponse)
                .toList();

        long totalElements = clienteService.countAll(nome, isActive);

        ClientePageResponse response = new ClientePageResponse(
                content,
                normalizedPage,
                normalizedSize,
                totalElements,
                Pagination.totalPages(totalElements, normalizedSize));

        return Response.ok(response).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed({"admin", "atendente"})
    @GET
    @Path("/{uuid}")
    @Operation(summary = "Buscar cliente por UUID", description = "Retorna os dados de um cliente especifico.")
    public Response findById(@PathParam("uuid") UUID uuid) {
        Cliente cliente = clienteService.findById(uuid);
        return Response.ok(clienteDtoMapper.toResponse(cliente)).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed({"admin", "atendente"})
    @PUT
    @Path("/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados cadastrais de um cliente. CPF e imutavel.")
    public Response update(@PathParam("uuid") UUID uuid, @Valid UpdateClienteRequest request) {
        UpdateClienteCommand command = clienteDtoMapper.toCommand(request);
        Cliente cliente = clienteService.update(uuid, command);
        return Response.ok(clienteDtoMapper.toResponse(cliente)).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Desativar cliente", description = "Executa soft delete do cliente (isActive=false).")
    public Response delete(@PathParam("uuid") UUID uuid) {
        clienteService.delete(uuid);
        return Response.noContent().build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @PATCH
    @Path("/{uuid}/reativar")
    @Operation(summary = "Reativar cliente", description = "Reativa um cliente previamente desativado.")
    public Response reactivate(@PathParam("uuid") UUID uuid) {
        clienteService.reactivate(uuid);
        return Response.noContent().build();
    }
}
