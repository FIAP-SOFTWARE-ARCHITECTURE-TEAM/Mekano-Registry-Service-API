package com.fiap.mekano.registry.infrastructure.mapper;

import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.valueobject.Endereco;
import com.fiap.mekano.registry.infrastructure.entity.ClienteEntity;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapeamento entre Cliente (dominio) e ClienteEntity (JPA).
 */
@ApplicationScoped
public class ClienteEntityMapper {

    public ClienteEntity toEntity(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        ClienteEntity entity = new ClienteEntity();
        entity.setUuid(cliente.getId());
        entity.setNome(cliente.getNome());
        entity.setCpf(cliente.getCpf().getValue());
        entity.setEmail(cliente.getEmail().getValue());
        entity.setTelefone(cliente.getTelefone() == null ? null : cliente.getTelefone().getValue());
        entity.setEnderecoLogradouro(cliente.getEndereco().getLogradouro());
        entity.setEnderecoNumero(cliente.getEndereco().getNumero());
        entity.setEnderecoComplemento(cliente.getEndereco().getComplemento());
        entity.setEnderecoBairro(cliente.getEndereco().getBairro());
        entity.setEnderecoCidade(cliente.getEndereco().getCidade());
        entity.setEnderecoUf(cliente.getEndereco().getUf());
        entity.setEnderecoCep(cliente.getEndereco().getCep());
        entity.setCreatedAt(cliente.getCreatedAt());
        entity.setIsActive(cliente.getIsActive());
        return entity;
    }

    public Cliente toDomain(ClienteEntity entity) {
        if (entity == null) {
            return null;
        }
        return Cliente.reconstitute(
                entity.getUuid(),
                entity.getNome(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getEnderecoLogradouro(),
                entity.getEnderecoNumero(),
                entity.getEnderecoComplemento(),
                entity.getEnderecoBairro(),
                entity.getEnderecoCidade(),
                entity.getEnderecoUf(),
                entity.getEnderecoCep(),
                entity.getCreatedAt(),
                entity.getIsActive());
    }

    public void updateEntity(Cliente cliente, ClienteEntity entity) {
        if (cliente == null || entity == null) {
            return;
        }
        Endereco endereco = cliente.getEndereco();
        entity.setNome(cliente.getNome());
        entity.setEmail(cliente.getEmail().getValue());
        entity.setTelefone(cliente.getTelefone() == null ? null : cliente.getTelefone().getValue());
        entity.setEnderecoLogradouro(endereco.getLogradouro());
        entity.setEnderecoNumero(endereco.getNumero());
        entity.setEnderecoComplemento(endereco.getComplemento());
        entity.setEnderecoBairro(endereco.getBairro());
        entity.setEnderecoCidade(endereco.getCidade());
        entity.setEnderecoUf(endereco.getUf());
        entity.setEnderecoCep(endereco.getCep());
    }
}
