# ADR-002 — PostgreSQL 16 com Flyway

**Status:** Aceita
**Fase:** 4
**Data:** 2026-11-26

---

## 1. Contexto

O Registry armazena dados mestres (clientes, veículos, peças e serviços) com:

- relacionamentos simples e chaves UUID expostas na API;
- unicidade apenas entre registros ativos (CPF, placa, código, nome);
- soft delete com `is_active` e `deleted_at`;
- auditoria (`created_at`, `updated_at`, `created_by`, `updated_by`);
- necessidade de migrations versionadas e reproduzíveis.

O monólito Mekano já usa PostgreSQL 16 e a equipe conhece o ecossistema (DBeaver, Flyway, Docker Compose).

---

## 2. Decisão

Usar **PostgreSQL 16** como banco do Registry, com **Flyway** para migrations.

### Ambiente local

- Container `registry-db` no `docker-compose.yml`;
- porta host `5433` (evita conflito com o PostgreSQL padrão na `5432`);
- database `mekano_registry`;
- usuário/senha padrão `mekano` / `mekano` (sobrescrevíveis por variáveis de ambiente).

### Configuração da aplicação

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=${REGISTRY_DB_URL:jdbc:postgresql://localhost:5433/mekano_registry}
quarkus.datasource.username=${REGISTRY_DB_USER:mekano}
quarkus.datasource.password=${REGISTRY_DB_PASSWORD:mekano}
quarkus.hibernate-orm.schema-management.strategy=none
quarkus.flyway.migrate-at-start=true
```

### Testes

- Testes de integração e REST usam **H2 em memória** com `MODE=PostgreSQL`;
- Flyway desabilitado nos testes (`quarkus.flyway.migrate-at-start=false`);
- schema criado por Hibernate (`drop-and-create`).

Motivo: o ambiente de desenvolvimento/CI não depende de Docker/Testcontainers para rodar a suíte. As migrations são validadas em PostgreSQL real no ambiente Docker e no PR.

---

## 3. Consequências

### Positivas

1. **Constraints e índices parciais** para unicidade entre ativos:

   ```sql
   CREATE UNIQUE INDEX ux_clientes_cpf_active
       ON clientes (cpf)
       WHERE is_active = TRUE AND deleted_at IS NULL;
   ```

2. **Transações ACID** e tipagem forte (`UUID`, `NUMERIC`, `BOOLEAN`, `TIMESTAMP`).
3. **Ferramental conhecido** pela equipe (DBeaver, `psql`, Docker).
4. **Migrations versionadas** e auditáveis.
5. **Paridade com o monólito** e com o serviço de Supplies.

### Negativas / custos

1. **Dependência de Docker** para o ambiente local completo (banco real).
2. **H2 não cobre 100% do PostgreSQL** (ex.: índices parciais, extensões).
3. **Flyway desabilitado em testes** exige validação adicional das migrations.
4. **Porta 5433** precisa ser documentada e configurável.

### Mitigações

- `docker-compose.yml` com healthcheck do banco.
- `.env.example` com todas as variáveis.
- Reset documentado (`docker compose down -v`).
- Migrations finais e revisadas (ver ADR-005).
- Testes REST e de repositório cobrindo regras de soft delete/reativação.

---

## 4. Alternativas consideradas

### Alternativa A — H2 também em produção (rejeitada)

- Simples para demonstração, mas sem recursos relacionais avançados e sem paridade com o monólito.
- **Motivo da rejeição:** o projeto exige PostgreSQL e migrations reais.

### Alternativa B — Testcontainers nos testes (rejeitada nesta fase)

- Maior fidelidade ao PostgreSQL.
- Exige Docker disponível em toda máquina de desenvolvimento e no runner.
- **Motivo da rejeição:** o build precisa ser executável sem Docker; a validação com PostgreSQL fica no ambiente Docker/PR.

### Alternativa C — MySQL/MariaDB (rejeitada)

- Não é o padrão do ecossistema Mekano.
- Índices parciais e recursos de `UUID` são menos aderentes ao caso.
- **Motivo da rejeição:** consistência com o monólito e com o Supplies.

---

## 5. Referências

- [`../03-modelo-de-dados.md`](../03-modelo-de-dados.md)
- [`../06-operacao-local.md`](../06-operacao-local.md)
- [`ADR-005-migrations-finais.md`](ADR-005-migrations-finais.md)
