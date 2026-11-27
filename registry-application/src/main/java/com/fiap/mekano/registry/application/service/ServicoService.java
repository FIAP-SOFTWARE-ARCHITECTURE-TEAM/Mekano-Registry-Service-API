package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Servico;
import com.fiap.mekano.registry.domain.port.in.CreateServicoCommand;
import com.fiap.mekano.registry.domain.port.in.ServicoServicePort;
import com.fiap.mekano.registry.domain.port.in.UpdateServicoCommand;
import com.fiap.mekano.registry.domain.port.out.ServicoRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ServicoService implements ServicoServicePort {

    private final ServicoRepositoryPort repository;

    public ServicoService(ServicoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Servico create(CreateServicoCommand command) {
        String nome = normalize(command.nome());
        Servico servico = Servico.create(nome, command.descricao(), command.valorUnitario());
        if (repository.existsByNome(nome)) {
            throw new AppException(409, Messages.get("servico.already.exists", nome));
        }
        return repository.create(servico);
    }

    @Override
    @Transactional
    public Servico update(UUID id, UpdateServicoCommand command) {
        Servico atual = findById(id);
        String nome = normalize(command.nome());
        Servico atualizado = Servico.reconstitute(
                atual.getId(),
                nome,
                command.descricao(),
                command.valorUnitario(),
                atual.getCreatedAt(),
                atual.getIsActive());
        if (repository.existsByNomeAndIdNot(nome, id)) {
            throw new AppException(409, Messages.get("servico.already.exists", nome));
        }
        return repository.update(atualizado);
    }

    @Override
    public Servico findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("servico.not.found", id)));
    }

    @Override
    public List<Servico> findAll(int page, int size, String sort, String nome, Boolean isActive) {
        return repository.findAll(page, size, sort, nome, isActive);
    }

    @Override
    public long countAll(String nome, Boolean isActive) {
        return repository.countAll(nome, isActive);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.markAsDeleted(id);
    }

    @Override
    @Transactional
    public void reactivate(UUID id) {
        Servico servico = findById(id);
        if (Boolean.FALSE.equals(servico.getIsActive())
                && repository.existsByNomeAndIdNot(servico.getNome(), id)) {
            throw new AppException(409, Messages.get("servico.already.exists", servico.getNome()));
        }
        repository.reactivate(id);
    }

    private static String normalize(String value) {
        return value == null ? null : value.strip();
    }
}
