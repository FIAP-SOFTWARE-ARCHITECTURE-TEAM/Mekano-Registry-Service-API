package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.port.in.ClienteServicePort;
import com.fiap.mekano.registry.domain.port.in.CreateClienteCommand;
import com.fiap.mekano.registry.domain.port.in.EnderecoCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateClienteCommand;
import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Casos de uso de Cliente.
 */
@ApplicationScoped
public class ClienteService implements ClienteServicePort {

    private final ClienteRepositoryPort clienteRepository;

    public ClienteService(ClienteRepositoryPort clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public Cliente execute(CreateClienteCommand command) {
        if (clienteRepository.existsByCpf(command.cpf())) {
            throw new AppException(409, Messages.get("cliente.already.exists", command.cpf()));
        }

        EnderecoCommand endereco = command.endereco();
        Cliente cliente = Cliente.create(
                command.nome(),
                command.cpf(),
                command.email(),
                command.telefone(),
                endereco == null ? null : endereco.logradouro(),
                endereco == null ? null : endereco.numero(),
                endereco == null ? null : endereco.complemento(),
                endereco == null ? null : endereco.bairro(),
                endereco == null ? null : endereco.cidade(),
                endereco == null ? null : endereco.uf(),
                endereco == null ? null : endereco.cep());

        return clienteRepository.create(cliente);
    }

    @Override
    @Transactional
    public Cliente update(UUID id, UpdateClienteCommand command) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("cliente.not.found", id)));

        EnderecoCommand endereco = command.endereco();
        cliente.atualizar(
                command.nome(),
                command.email(),
                command.telefone(),
                endereco == null ? null : endereco.logradouro(),
                endereco == null ? null : endereco.numero(),
                endereco == null ? null : endereco.complemento(),
                endereco == null ? null : endereco.bairro(),
                endereco == null ? null : endereco.cidade(),
                endereco == null ? null : endereco.uf(),
                endereco == null ? null : endereco.cep());

        return clienteRepository.update(cliente);
    }

    @Override
    public Cliente findById(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("cliente.not.found", id)));
    }

    @Override
    public List<Cliente> findAll(int page, int size, String sort, String nome, Boolean isActive) {
        return clienteRepository.findAll(page, size, sort, nome, isActive);
    }

    @Override
    public long countAll(String nome, Boolean isActive) {
        return clienteRepository.countAll(nome, isActive);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        clienteRepository.markAsDeleted(id);
    }

    @Override
    @Transactional
    public void reactivate(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("cliente.not.found", id)));

        if (Boolean.FALSE.equals(cliente.getIsActive())
                && clienteRepository.existsByCpf(cliente.getCpf().getValue())) {
            throw new AppException(409, Messages.get("cliente.already.exists", cliente.getCpf().getValue()));
        }

        clienteRepository.reactivate(id);
    }
}
