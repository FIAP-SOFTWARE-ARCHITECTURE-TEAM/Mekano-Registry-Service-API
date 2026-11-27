package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Servico;
import com.fiap.mekano.registry.domain.port.out.ServicoRepositoryPort;
import com.fiap.mekano.registry.infrastructure.entity.ServicoEntity;
import com.fiap.mekano.registry.infrastructure.mapper.ServicoEntityMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class ServicoRepositoryImpl implements ServicoRepositoryPort {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("nome", "valorUnitario", "createdAt");
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ServicoPanacheRepository panacheRepository;
    private final ServicoEntityMapper mapper;

    public ServicoRepositoryImpl(ServicoPanacheRepository panacheRepository, ServicoEntityMapper mapper) {
        this.panacheRepository = panacheRepository;
        this.mapper = mapper;
    }

    @Override
    public Servico create(Servico servico) {
        ServicoEntity entity = mapper.toEntity(servico);
        try {
            panacheRepository.persist(entity);
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("servico.already.exists", servico.getNome()), e);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Servico update(Servico servico) {
        ServicoEntity entity = findEntity(servico.getId());
        mapper.updateEntity(servico, entity);
        try {
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("servico.already.exists", servico.getNome()), e);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Servico> findById(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByNome(String nome) {
        return panacheRepository.count("nome = ?1 and isActive = ?2", nome, true) > 0;
    }

    @Override
    public boolean existsByNomeAndIdNot(String nome, UUID id) {
        return panacheRepository.count("nome = ?1 and uuid <> ?2 and isActive = ?3", nome, id, true) > 0;
    }

    @Override
    public List<Servico> findAll(int page, int size, String sort, String nome, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        String query = buildFilter(nome, isActive, params);
        PanacheQuery<ServicoEntity> result = panacheRepository.find(query, parseSort(sort), params);
        return result.page(Page.of(Math.max(page, 0), normalizeSize(size)))
                .list()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countAll(String nome, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        return panacheRepository.count(buildFilter(nome, isActive, params), params);
    }

    @Override
    public void markAsDeleted(UUID id) {
        ServicoEntity entity = findEntity(id);
        entity.setIsActive(false);
        entity.setDeletedAt(LocalDateTime.now());
        panacheRepository.flush();
    }

    @Override
    public void reactivate(UUID id) {
        ServicoEntity entity = findEntity(id);
        entity.setIsActive(true);
        entity.setDeletedAt(null);
        try {
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("servico.already.exists", entity.getNome()), e);
        }
    }

    private ServicoEntity findEntity(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .orElseThrow(() -> new AppException(404, Messages.get("servico.not.found", id)));
    }

    private static String buildFilter(String nome, Boolean isActive, Map<String, Object> params) {
        StringBuilder query = new StringBuilder("1 = 1");
        if (isActive != null) {
            query.append(" and isActive = :isActive");
            params.put("isActive", isActive);
        }
        if (nome != null && !nome.isBlank()) {
            query.append(" and lower(nome) like :nome");
            params.put("nome", "%" + nome.strip().toLowerCase(Locale.ROOT) + "%");
        }
        return query.toString();
    }

    private static int normalizeSize(int size) {
        return size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private static Sort parseSort(String sort) {
        String value = sort == null || sort.isBlank() ? "nome,asc" : sort;
        String[] parts = value.split(",", 2);
        String field = ALLOWED_SORT_FIELDS.contains(parts[0].strip()) ? parts[0].strip() : "nome";
        boolean ascending = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].strip());
        return ascending ? Sort.by(field).ascending() : Sort.by(field).descending();
    }
}
