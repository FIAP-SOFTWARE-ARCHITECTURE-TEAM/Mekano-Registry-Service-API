package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.domain.port.in.CreatePecaCommand;
import com.fiap.mekano.registry.domain.port.in.PecaServicePort;
import com.fiap.mekano.registry.domain.port.in.UpdatePecaCommand;
import com.fiap.mekano.registry.domain.port.out.PecaRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PecaService implements PecaServicePort {

    private final PecaRepositoryPort repository;

    public PecaService(PecaRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Peca create(CreatePecaCommand command) {
        String codigo = normalize(command.codigo());
        Peca peca = Peca.create(codigo, command.descricao(), command.valorUnitario());
        if (repository.existsByCodigo(codigo)) {
            throw new AppException(409, Messages.get("peca.already.exists", codigo));
        }
        return repository.create(peca);
    }

    @Override
    @Transactional
    public Peca update(UUID id, UpdatePecaCommand command) {
        Peca atual = findById(id);
        String codigo = normalize(command.codigo());
        Peca atualizada = Peca.reconstitute(
                atual.getId(),
                codigo,
                command.descricao(),
                command.valorUnitario(),
                atual.getCreatedAt(),
                atual.getIsActive());
        if (repository.existsByCodigoAndIdNot(codigo, id)) {
            throw new AppException(409, Messages.get("peca.already.exists", codigo));
        }
        return repository.update(atualizada);
    }

    @Override
    public Peca findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(404, Messages.get("peca.not.found", id)));
    }

    @Override
    public List<Peca> findByIds(Collection<UUID> ids) {
        return repository.findByIds(ids);
    }

    @Override
    public List<Peca> findAll(int page, int size, String sort, String codigo, String descricao, Boolean isActive) {
        return repository.findAll(page, size, sort, codigo, descricao, isActive);
    }

    @Override
    public long countAll(String codigo, String descricao, Boolean isActive) {
        return repository.countAll(codigo, descricao, isActive);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.markAsDeleted(id);
    }

    @Override
    @Transactional
    public void reactivate(UUID id) {
        Peca peca = findById(id);
        if (Boolean.FALSE.equals(peca.getIsActive()) && repository.existsByCodigoAndIdNot(peca.getCodigo(), id)) {
            throw new AppException(409, Messages.get("peca.already.exists", peca.getCodigo()));
        }
        repository.reactivate(id);
    }

    private static String normalize(String value) {
        return value == null ? null : value.strip();
    }
}
