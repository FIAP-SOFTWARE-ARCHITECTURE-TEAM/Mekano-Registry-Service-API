package com.fiap.mekano.registry.infrastructure.repository;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import com.fiap.mekano.registry.infrastructure.entity.ClienteEntity;
import com.fiap.mekano.registry.infrastructure.mapper.ClienteEntityMapper;
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
public class ClienteRepositoryImpl implements ClienteRepositoryPort {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("nome", "email", "createdAt");
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ClientePanacheRepository panacheRepository;
    private final ClienteEntityMapper mapper;

    public ClienteRepositoryImpl(ClientePanacheRepository panacheRepository, ClienteEntityMapper mapper) {
        this.panacheRepository = panacheRepository;
        this.mapper = mapper;
    }

    @Override
    public Cliente create(Cliente cliente) {
        ClienteEntity entity = mapper.toEntity(cliente);
        try {
            panacheRepository.persist(entity);
            panacheRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new AppException(409, Messages.get("cliente.already.exists", cliente.getCpf().getValue()), e);
        }
        return mapper.toDomain(entity);
    }

    @Override
    public Cliente update(Cliente cliente) {
        ClienteEntity entity = findByUuid(cliente.getId());
        mapper.updateEntity(cliente, entity);
        panacheRepository.flush();
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Cliente> findById(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Cliente> findByCpf(String cpf) {
        String digits = onlyDigits(cpf);
        if (digits == null) {
            return Optional.empty();
        }
        return panacheRepository.find("cpf = ?1 and isActive = ?2", digits, true)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        String digits = onlyDigits(cpf);
        if (digits == null) {
            return false;
        }
        return panacheRepository.count("cpf = ?1 and isActive = ?2", digits, true) > 0;
    }

    @Override
    public List<Cliente> findAll(int page, int size, String sort, String nome, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        String query = buildFilter(nome, isActive, params);

        PanacheQuery<ClienteEntity> panacheQuery = panacheRepository.find(query, parseSort(sort), params);
        return panacheQuery.page(Page.of(normalizePage(page), normalizeSize(size)))
                .list()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countAll(String nome, Boolean isActive) {
        Map<String, Object> params = new HashMap<>();
        String query = buildFilter(nome, isActive, params);
        return panacheRepository.count(query, params);
    }

    @Override
    public void markAsDeleted(UUID id) {
        ClienteEntity entity = findByUuid(id);
        entity.setIsActive(false);
        entity.setDeletedAt(LocalDateTime.now());
        panacheRepository.flush();
    }

    @Override
    public void reactivate(UUID id) {
        ClienteEntity entity = findByUuid(id);
        entity.setIsActive(true);
        entity.setDeletedAt(null);
        panacheRepository.flush();
    }

    private ClienteEntity findByUuid(UUID id) {
        return panacheRepository.find("uuid = ?1", id)
                .firstResultOptional()
                .orElseThrow(() -> new AppException(404, Messages.get("cliente.not.found", id)));
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

    private static String onlyDigits(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.strip().replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
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
        String value = sort == null || sort.isBlank() ? "nome,asc" : sort;
        String[] parts = value.split(",", 2);
        String field = ALLOWED_SORT_FIELDS.contains(parts[0].strip()) ? parts[0].strip() : "nome";
        boolean ascending = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].strip());
        return ascending ? Sort.by(field).ascending() : Sort.by(field).descending();
    }
}
