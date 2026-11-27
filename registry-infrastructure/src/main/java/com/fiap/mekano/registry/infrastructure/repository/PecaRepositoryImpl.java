package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.domain.port.out.PecaRepositoryPort;
import com.fiap.mekano.registry.infrastructure.entity.PecaEntity;
import com.fiap.mekano.registry.infrastructure.mapper.PecaEntityMapper;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class PecaRepositoryImpl implements PecaRepositoryPort {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("codigo", "descricao", "valorUnitario", "createdAt");
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final PecaPanacheRepository panacheRepository;
    private final PecaEntityMapper mapper;

    public PecaRepositoryImpl(PecaPanacheRepository panacheRepository, PecaEntityMapper mapper) {
        this.panacheRepository = panacheRepository;
        this.mapper = mapper;
    }

    @Override
    public Peca create(Peca peca) {
        PecaEntity entity = mapper.toEntity(peca);
        try {
            panacheRepository.persist(entity);
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("peca.already.exists", peca.getCodigo()), e);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Peca update(Peca peca) {
        PecaEntity entity = findEntity(peca.getId());
        mapper.updateEntity(peca, entity);
        try {
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("peca.already.exists", peca.getCodigo()), e);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Peca> findById(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<Peca> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return panacheRepository.find("uuid in ?1", ids)
                .list()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return panacheRepository.count("codigo = ?1 and isActive = ?2", codigo, true) > 0;
    }

    @Override
    public boolean existsByCodigoAndIdNot(String codigo, UUID id) {
        return panacheRepository.count("codigo = ?1 and uuid <> ?2 and isActive = ?3", codigo, id, true) > 0;
    }

    @Override
    public List<Peca> findAll(int page, int size, String sort, String codigo, String descricao, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        String query = buildFilter(codigo, descricao, isActive, params);
        PanacheQuery<PecaEntity> result = panacheRepository.find(query, parseSort(sort), params);
        return result.page(Page.of(Math.max(page, 0), normalizeSize(size)))
                .list()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countAll(String codigo, String descricao, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        return panacheRepository.count(buildFilter(codigo, descricao, isActive, params), params);
    }

    @Override
    public void markAsDeleted(UUID id) {
        PecaEntity entity = findEntity(id);
        entity.setIsActive(false);
        entity.setDeletedAt(LocalDateTime.now());
        panacheRepository.flush();
    }

    @Override
    public void reactivate(UUID id) {
        PecaEntity entity = findEntity(id);
        entity.setIsActive(true);
        entity.setDeletedAt(null);
        try {
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("peca.already.exists", entity.getCodigo()), e);
        }
    }

    private PecaEntity findEntity(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .orElseThrow(() -> new AppException(404, Messages.get("peca.not.found", id)));
    }

    private static String buildFilter(String codigo, String descricao, Boolean isActive, Map<String, Object> params) {
        StringBuilder query = new StringBuilder("1 = 1");
        if (isActive != null) {
            query.append(" and isActive = :isActive");
            params.put("isActive", isActive);
        }
        addContainsFilter(query, params, "codigo", codigo);
        addContainsFilter(query, params, "descricao", descricao);
        return query.toString();
    }

    private static void addContainsFilter(
            StringBuilder query, Map<String, Object> params, String field, String value) {
        if (value != null && !value.isBlank()) {
            query.append(" and lower(").append(field).append(") like :").append(field);
            params.put(field, "%" + value.strip().toLowerCase(Locale.ROOT) + "%");
        }
    }

    private static int normalizeSize(int size) {
        return size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private static Sort parseSort(String sort) {
        String value = sort == null || sort.isBlank() ? "codigo,asc" : sort;
        String[] parts = value.split(",", 2);
        String field = ALLOWED_SORT_FIELDS.contains(parts[0].strip()) ? parts[0].strip() : "codigo";
        boolean ascending = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].strip());
        return ascending ? Sort.by(field).ascending() : Sort.by(field).descending();
    }
}
