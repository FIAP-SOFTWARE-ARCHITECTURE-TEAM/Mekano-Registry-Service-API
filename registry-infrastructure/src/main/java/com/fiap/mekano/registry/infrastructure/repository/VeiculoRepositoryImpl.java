package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Veiculo;
import com.fiap.mekano.registry.domain.port.out.VeiculoRepositoryPort;
import com.fiap.mekano.registry.infrastructure.entity.VeiculoEntity;
import com.fiap.mekano.registry.infrastructure.mapper.VeiculoEntityMapper;
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
public class VeiculoRepositoryImpl implements VeiculoRepositoryPort {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("placa", "marca", "modelo", "ano", "createdAt");
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final VeiculoPanacheRepository panacheRepository;
    private final VeiculoEntityMapper mapper;

    public VeiculoRepositoryImpl(VeiculoPanacheRepository panacheRepository, VeiculoEntityMapper mapper) {
        this.panacheRepository = panacheRepository;
        this.mapper = mapper;
    }

    @Override
    public Veiculo create(Veiculo veiculo) {
        VeiculoEntity entity = mapper.toEntity(veiculo);
        try {
            panacheRepository.persist(entity);
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("veiculo.placa.already.exists", veiculo.getPlaca().getValue()), e);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Veiculo update(Veiculo veiculo) {
        VeiculoEntity entity = findByUuid(veiculo.getId());
        mapper.updateEntity(veiculo, entity);
        panacheRepository.flush();
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Veiculo> findById(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Veiculo> findByPlaca(String placa) {
        String normalized = normalizePlaca(placa);
        if (normalized == null) {
            return Optional.empty();
        }
        return panacheRepository.find("placa = ?1 and isActive = ?2", normalized, true)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByPlaca(String placa) {
        String normalized = normalizePlaca(placa);
        if (normalized == null) {
            return false;
        }
        return panacheRepository.count("placa = ?1 and isActive = ?2", normalized, true) > 0;
    }

    @Override
    public List<Veiculo> findAll(int page, int size, String sort, UUID clienteUuid, String placa, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        String query = buildFilter(clienteUuid, placa, isActive, params);

        PanacheQuery<VeiculoEntity> panacheQuery = panacheRepository.find(query, parseSort(sort), params);
        return panacheQuery.page(Page.of(normalizePage(page), normalizeSize(size)))
                .list()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countAll(UUID clienteUuid, String placa, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        String query = buildFilter(clienteUuid, placa, isActive, params);
        return panacheRepository.count(query, params);
    }

    @Override
    public void markAsDeleted(UUID id) {
        VeiculoEntity entity = findByUuid(id);
        entity.setIsActive(false);
        entity.setDeletedAt(LocalDateTime.now());
        panacheRepository.flush();
    }

    @Override
    public void reactivate(UUID id) {
        VeiculoEntity entity = findByUuid(id);
        entity.setIsActive(true);
        entity.setDeletedAt(null);
        panacheRepository.flush();
    }

    private VeiculoEntity findByUuid(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .orElseThrow(() -> new AppException(404, Messages.get("veiculo.not.found", id)));
    }

    private static String buildFilter(UUID clienteUuid, String placa, Boolean isActive, Map<String, Object> params) {
        StringBuilder query = new StringBuilder("1 = 1");
        if (clienteUuid != null) {
            query.append(" and clienteUuid = :clienteUuid");
            params.put("clienteUuid", clienteUuid);
        }
        if (isActive != null) {
            query.append(" and isActive = :isActive");
            params.put("isActive", isActive);
        }
        if (placa != null && !placa.isBlank()) {
            query.append(" and lower(placa) like :placa");
            params.put("placa", "%" + placa.strip().toLowerCase(Locale.ROOT).replace("-", "") + "%");
        }
        return query.toString();
    }

    private static String normalizePlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return null;
        }
        return placa.strip().toUpperCase(Locale.ROOT).replace("-", "");
    }

    private static int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private static int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private static Sort parseSort(String sort) {
        String value = sort == null || sort.isBlank() ? "placa,asc" : sort;
        String[] parts = value.split(",", 2);
        String field = ALLOWED_SORT_FIELDS.contains(parts[0].strip()) ? parts[0].strip() : "placa";
        boolean ascending = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].strip());
        return ascending ? Sort.by(field).ascending() : Sort.by(field).descending();
    }
}
