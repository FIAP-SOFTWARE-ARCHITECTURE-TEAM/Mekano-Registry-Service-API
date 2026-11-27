package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.model.Veiculo;
import com.fiap.mekano.registry.domain.port.in.CreateVeiculoCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateVeiculoCommand;
import com.fiap.mekano.registry.domain.port.in.VeiculoServicePort;
import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import com.fiap.mekano.registry.domain.port.out.VeiculoRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Casos de uso de Veiculo.
 */
@ApplicationScoped
public class VeiculoService implements VeiculoServicePort {

    private final VeiculoRepositoryPort veiculoRepository;
    private final ClienteRepositoryPort clienteRepository;

    public VeiculoService(VeiculoRepositoryPort veiculoRepository, ClienteRepositoryPort clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public Veiculo execute(CreateVeiculoCommand command) {
        Cliente cliente = clienteRepository.findById(command.clienteUuid())
                .orElseThrow(() -> new AppException(404, Messages.get("cliente.not.found", command.clienteUuid())));

        if (Boolean.FALSE.equals(cliente.getIsActive())) {
            throw new AppException(409, Messages.get("cliente.inactive", command.clienteUuid()));
        }

        if (veiculoRepository.existsByPlaca(command.placa())) {
            throw new AppException(409, Messages.get("veiculo.placa.already.exists", command.placa()));
        }

        Veiculo veiculo = Veiculo.create(
                command.clienteUuid(),
                command.placa(),
                command.marca(),
                command.modelo(),
                command.ano());

        return veiculoRepository.create(veiculo);
    }

    @Override
    @Transactional
    public Veiculo update(UUID id, UpdateVeiculoCommand command) {
        Veiculo existente = veiculoRepository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("veiculo.not.found", id)));

        Veiculo atualizado = Veiculo.reconstitute(
                existente.getId(),
                existente.getClienteUuid(),
                existente.getPlaca().getValue(),
                Optional.ofNullable(command.marca()).filter(v -> !v.isBlank()).orElse(existente.getMarca()),
                Optional.ofNullable(command.modelo()).filter(v -> !v.isBlank()).orElse(existente.getModelo()),
                Optional.ofNullable(command.ano()).orElse(existente.getAno()),
                existente.getCreatedAt(),
                existente.getIsActive());

        return veiculoRepository.update(atualizado);
    }

    @Override
    public Veiculo findById(UUID id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("veiculo.not.found", id)));
    }

    @Override
    public List<Veiculo> findAll(int page, int size, String sort, UUID clienteUuid, String placa, Boolean isActive) {
        return veiculoRepository.findAll(page, size, sort, clienteUuid, placa, isActive);
    }

    @Override
    public long countAll(UUID clienteUuid, String placa, Boolean isActive) {
        return veiculoRepository.countAll(clienteUuid, placa, isActive);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        veiculoRepository.markAsDeleted(id);
    }

    @Override
    @Transactional
    public void reactivate(UUID id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("veiculo.not.found", id)));

        if (Boolean.FALSE.equals(veiculo.getIsActive())
                && veiculoRepository.existsByPlaca(veiculo.getPlaca().getValue())) {
            throw new AppException(409, Messages.get("veiculo.placa.already.exists", veiculo.getPlaca().getValue()));
        }

        veiculoRepository.reactivate(id);
    }
}
