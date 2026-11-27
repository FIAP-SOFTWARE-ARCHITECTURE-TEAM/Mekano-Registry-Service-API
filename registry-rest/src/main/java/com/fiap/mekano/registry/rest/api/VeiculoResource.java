package com.fiap.mekano.registry.rest.api;

import com.fiap.mekano.registry.domain.model.Veiculo;
import com.fiap.mekano.registry.domain.port.in.CreateVeiculoCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateVeiculoCommand;
import com.fiap.mekano.registry.domain.port.in.VeiculoServicePort;
import com.fiap.mekano.registry.rest.dto.CreateVeiculoRequest;
import com.fiap.mekano.registry.rest.dto.UpdateVeiculoRequest;
import com.fiap.mekano.registry.rest.dto.VeiculoPageResponse;
import com.fiap.mekano.registry.rest.dto.VeiculoResponse;
import com.fiap.mekano.registry.rest.mapper.VeiculoDtoMapper;
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
 * Endpoints de veiculos. Transacoes ficam na camada de aplicacao.
 */
@Path("/veiculos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Veiculos", description = "Gerenciamento de veiculos")
public class VeiculoResource {

    private final VeiculoServicePort veiculoService;
    private final VeiculoDtoMapper veiculoDtoMapper;

    @Inject
    public VeiculoResource(VeiculoServicePort veiculoService, VeiculoDtoMapper veiculoDtoMapper) {
        this.veiculoService = veiculoService;
        this.veiculoDtoMapper = veiculoDtoMapper;
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed({"admin", "atendente"})
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cadastrar veiculo", description = "Cadastra um veiculo vinculado a um cliente existente e ativo.")
    public Response create(@Valid CreateVeiculoRequest request, @Context UriInfo uriInfo) {
        CreateVeiculoCommand command = veiculoDtoMapper.toCommand(request);
        Veiculo veiculo = veiculoService.execute(command);
        VeiculoResponse response = veiculoDtoMapper.toResponse(veiculo);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(response.uuid().toString())
                .build();

        return Response.created(location).entity(response).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed({"admin", "atendente"})
    @GET
    @Operation(summary = "Listar veiculos", description = "Lista veiculos com paginacao e filtros por cliente, placa e status.")
    public Response listAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sort") @DefaultValue("placa,asc") String sort,
            @QueryParam("clienteUuid") UUID clienteUuid,
            @QueryParam("placa") String placa,
            @QueryParam("isActive") Boolean isActive) {

        int normalizedPage = Pagination.page(page);
        int normalizedSize = Pagination.size(size);

        List<VeiculoResponse> content = veiculoService
                .findAll(normalizedPage, normalizedSize, sort, clienteUuid, placa, isActive)
                .stream()
                .map(veiculoDtoMapper::toResponse)
                .toList();

        long totalElements = veiculoService.countAll(clienteUuid, placa, isActive);

        VeiculoPageResponse response = new VeiculoPageResponse(
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
    @Operation(summary = "Buscar veiculo por UUID", description = "Retorna os dados de um veiculo especifico.")
    public Response findById(@PathParam("uuid") UUID uuid) {
        Veiculo veiculo = veiculoService.findById(uuid);
        return Response.ok(veiculoDtoMapper.toResponse(veiculo)).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed({"admin", "atendente"})
    @PUT
    @Path("/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualizar veiculo", description = "Atualiza marca, modelo e ano. Placa e cliente sao imutaveis.")
    public Response update(@PathParam("uuid") UUID uuid, @Valid UpdateVeiculoRequest request) {
        UpdateVeiculoCommand command = veiculoDtoMapper.toCommand(request);
        Veiculo veiculo = veiculoService.update(uuid, command);
        return Response.ok(veiculoDtoMapper.toResponse(veiculo)).build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Desativar veiculo", description = "Executa soft delete do veiculo (isActive=false).")
    public Response delete(@PathParam("uuid") UUID uuid) {
        veiculoService.delete(uuid);
        return Response.noContent().build();
    }

    // TODO: habilitar quando autenticacao entrar no escopo
    // @RolesAllowed("admin")
    @PATCH
    @Path("/{uuid}/reativar")
    @Operation(summary = "Reativar veiculo", description = "Reativa um veiculo previamente desativado.")
    public Response reactivate(@PathParam("uuid") UUID uuid) {
        veiculoService.reactivate(uuid);
        return Response.noContent().build();
    }
}
