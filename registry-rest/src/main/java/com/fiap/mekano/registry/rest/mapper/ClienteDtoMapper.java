package com.fiap.mekano.registry.rest.mapper;

import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.port.in.CreateClienteCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateClienteCommand;
import com.fiap.mekano.registry.domain.valueobject.Endereco;
import com.fiap.mekano.registry.rest.dto.ClienteResponse;
import com.fiap.mekano.registry.rest.dto.CreateClienteRequest;
import com.fiap.mekano.registry.rest.dto.EnderecoResponse;
import com.fiap.mekano.registry.rest.dto.UpdateClienteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapeamento entre DTOs REST e o modelo de dominio de Cliente.
 */
@Mapper(componentModel = "cdi")
public interface ClienteDtoMapper {

    CreateClienteCommand toCommand(CreateClienteRequest request);

    UpdateClienteCommand toCommand(UpdateClienteRequest request);

    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "cpf", expression = "java(cliente.getCpf().getValue())")
    @Mapping(target = "email", expression = "java(cliente.getEmail().getValue())")
    @Mapping(target = "telefone", expression = "java(cliente.getTelefone() == null ? null : cliente.getTelefone().getValue())")
    @Mapping(target = "endereco", expression = "java(toEnderecoResponse(cliente.getEndereco()))")
    ClienteResponse toResponse(Cliente cliente);

    default EnderecoResponse toEnderecoResponse(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoResponse(
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf(),
                endereco.getCep());
    }
}
