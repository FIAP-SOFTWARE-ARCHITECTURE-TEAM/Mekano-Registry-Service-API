# Mekano Registry Service API

API de cadastro e catálogo do projeto **Mekano** — **Fase 4**.

> **Status atual:** Sprints 0–3 implementados — fundação, CRUDs, batch de peças, seed, OpenAPI e coleção Postman base. Build e suíte local verificados.

> **Prefixo de todos os endpoints:** `/api/v4`
>
> Exemplo: `GET http://localhost:8081/api/v4/clientes`

### Status dos sprints

| Sprint | Escopo | Status |
|---|---|---|
| 0 | Fundação: POMs, compose, Dockerfile, CI e ADRs | ✅ Implementado |
| 1 | Clientes e veículos | ✅ Implementado |
| 2 | Peças, serviços e batch interno | ✅ Implementado |
| 3 | Seed, OpenAPI, Postman base e documentação | ✅ Implementado |
| 4 | Integração, hardening e release | ⏳ Pendente |
| 5 | Testes e Postman | ⏳ Pendente |

---

## 1. O que este serviço faz

O Registry é o serviço de **dados mestres e catálogo** do Mekano. Ele é responsável por:

- CRUD de **clientes**;
- CRUD de **veículos**;
- CRUD de **peças/insumos** (somente catálogo: código, descrição e valor);
- CRUD de **serviços**;
- Endpoint interno de **consulta em lote de peças** para o serviço de Supplies.

## 2. O que este serviço NÃO faz

- **Estoque** (saldo atual, saldo reservado, estoque mínimo e movimentos) — pertence ao `Mekano-Supplies-Purchase-Request-Service-API`;
- Requisição de compra;
- NF de entrada;
- Ordens de serviço, orçamentos, pagamentos, WhatsApp e autenticação.

A separação é intencional: o Registry é **catálogo**; o Supplies é **estoque + compras + NF**.
A decisão está registrada em [`docs/adr/ADR-003-catalogo-de-pecas.md`](docs/adr/ADR-003-catalogo-de-pecas.md).

---

## 3. Stack

| Item | Versão/uso |
|---|---|
| Java | 17 |
| Quarkus | 3.36.0 |
| Maven | Multi-módulo (wrapper) |
| PostgreSQL | 16 |
| Flyway | Migrations |
| Hibernate ORM / Panache | Persistência |
| MapStruct | Mapeamento |
| Lombok | Redução de boilerplate |
| SmallRye OpenAPI | Swagger/OpenAPI |
| JUnit 5 / AssertJ / Mockito / RestAssured | Testes |

## 4. Arquitetura

Clean Architecture, com os mesmos padrões do monólito Mekano:

```text
registry-rest            (Quarkus app / resources / DTOs)
  ├── registry-application (services / use cases)
  ├── registry-infrastructure (JPA / repos / mappers / migrations)
  └── registry-domain (models / VOs / ports / exceptions)
```

- Pacotes base: `com.fiap.mekano.registry.*`
- Prefixo REST: `quarkus.rest.path=/api/v4`
- Banco: `mekano_registry`
- Porta da API: `8081`
- Porta do PostgreSQL: `5433`

Detalhes em [`docs/01-arquitetura.md`](docs/01-arquitetura.md).

---

## 5. Como rodar localmente

### 5.1 Pré-requisitos

- Java 17
- Docker Desktop
- Maven Wrapper (`./mvnw` / `mvnw.cmd`)

### 5.2 Subir banco + API com Docker Compose

```bash
docker compose up -d --build
```

A API ficará disponível em:

- API: `http://localhost:8081/api/v4`
- Swagger UI: `http://localhost:8081/q/swagger-ui`
- Health: `http://localhost:8081/q/health`
- Métricas: `http://localhost:8081/q/metrics`

### 5.3 Rodar em modo dev (Maven)

```bash
docker compose up -d registry-db
./mvnw quarkus:dev -pl registry-rest -am
```

No Windows:

```powershell
docker compose up -d registry-db
.\mvnw.cmd quarkus:dev -pl registry-rest -am
```

### 5.4 Resetar banco e reaplicar migrations/seed

```bash
docker compose down -v
docker compose up -d --build
```

> As migrations são **finais** (sem migrations de correção). Durante a fase acadêmica, alterações de schema devem ser feitas na migration original e o volume recriado.

---

## 6. Banco de dados e DBeaver

Após subir os containers:

| Configuração | Valor |
|---|---|
| Driver | PostgreSQL |
| Host | `localhost` |
| Porta | `5433` |
| Database | `mekano_registry` |
| Usuário | `mekano` |
| Senha | `mekano` |

Passo a passo no DBeaver:

1. **Database → New Database Connection → PostgreSQL**.
2. Preencha host, porta, database, usuário e senha conforme a tabela.
3. Teste a conexão e finalize.
4. As tabelas estarão no schema `public`.

Detalhes em [`docs/06-operacao-local.md`](docs/06-operacao-local.md).

---

## 7. Seed inicial

A migration `V5__seed_dados_iniciais.sql` cria dados de demonstração com UUIDs fixos:

| Entidade | UUID |
|---|---|
| Cliente | `11111111-1111-1111-1111-111111111111` |
| Veículo | `22222222-2222-2222-2222-222222222222` |
| Serviço — Troca de Óleo | `33333333-3333-3333-3333-333333333333` |
| Serviço — Alinhamento e Balanceamento | `44444444-4444-4444-4444-444444444444` |
| Peça — `PEA-001` | `55555555-5555-5555-5555-555555555555` |
| Peça — `PEA-002` | `66666666-6666-6666-6666-666666666666` |

> O seed do Registry cria apenas o **catálogo** das peças. O estoque inicial das mesmas peças é criado pelo seed do Supplies (`V6__seed_estoque_inicial.sql`).

---

## 8. Endpoints

### 8.1 Públicos

| Método | Endpoint | Descrição |
|---|---|---|
| POST/GET/PUT/DELETE/PATCH | `/api/v4/clientes` | CRUD de clientes; `PATCH /{uuid}/reativar` |
| POST/GET/PUT/DELETE/PATCH | `/api/v4/veiculos` | CRUD de veículos; `PATCH /{uuid}/reativar` |
| POST/GET/PUT/DELETE/PATCH | `/api/v4/pecas` | CRUD de peças (catálogo) |
| POST/GET/PUT/DELETE/PATCH | `/api/v4/servicos` | CRUD de serviços |

Filtros e paginação dos endpoints implementados:

- `GET /api/v4/clientes?page=0&size=20&nome=Cliente&isActive=true`
- `GET /api/v4/veiculos?page=0&size=20&clienteUuid=...&placa=ABC1D23&isActive=true`
- `GET /api/v4/pecas?page=0&size=20&codigo=PEA&descricao=Filtro&isActive=true`
- `GET /api/v4/servicos?page=0&size=20&nome=Troca&isActive=true`
- `page` default `0`, `size` default `20` (máximo `100`), resposta com `content`, `page`, `size`, `totalElements` e `totalPages`.
- Erros no padrão RFC 7807 (`application/problem+json`).

### 8.2 Interno

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v4/internal/pecas/batch` | Consulta em lote de peças para o Supplies |

Contratos completos em [`docs/04-contratos-api.md`](docs/04-contratos-api.md).

O batch recebe de 1 a 100 UUIDs e retorna `pecas` e `naoEncontradas`; peças inativas são incluídas com `isActive=false`.
OpenAPI: `http://localhost:8081/q/openapi`; Swagger UI: `http://localhost:8081/q/swagger-ui`.

---

## 9. Roles (futuras, comentadas)

A autenticação está **fora do escopo** desta fase. Os endpoints terão `@RolesAllowed` comentado com `TODO`:

| Endpoint | Roles propostas |
|---|---|
| Clientes/veículos (leitura/escrita) | `admin`, `atendente` |
| Clientes/veículos (exclusão/reativação) | `admin` |
| Peças/serviços | `admin` |
| Batch interno de peças | `service:supplies-purchase` |

Detalhes em [`docs/07-decisoes-e-riscos.md`](docs/07-decisoes-e-riscos.md).

---

## 10. Testes e Postman

```bash
./mvnw -B verify          # build completo + testes
./mvnw test -pl registry-domain      # VOs e entidades (<3s)
./mvnw test -pl registry-application # services com Mockito
./mvnw test -pl registry-infrastructure # repositórios com H2
./mvnw test -pl registry-rest        # REST Assured + health/OpenAPI
```

- Testes unitários, de integração e de REST seguem o padrão do monólito.
- Integração e REST usam H2 em memória (`MODE=PostgreSQL`) para não depender de Docker no build.
- O teste do seed reaplica V5 e confirma os seis registros sem duplicação; V1–V5 foram verificadas em PostgreSQL 16 descartável.
- A coleção Postman base usa o environment `Mekano Registry Local` e pode ser executada em modo somente leitura após aplicar o seed.
- Coleção: [`docs/postman/Mekano-Registry.postman_collection.json`](docs/postman/Mekano-Registry.postman_collection.json).
- Environment: [`docs/postman/Mekano-Registry-Local.postman_environment.json`](docs/postman/Mekano-Registry-Local.postman_environment.json).
- A **migração completa dos testes automatizados** das pastas `/test` do monólito e a expansão final dos cenários Postman serão feitas na branch:
  - `test/sprint-5-testes-e-postman`
- Durante os sprints 1–4, cada PR mantém o build verde com testes mínimos das novas classes.
- Estratégia completa em [`docs/05-testes-e-postman.md`](docs/05-testes-e-postman.md).

---

## 11. Branches e fluxo de entrega

| Branch | Sprint | Objetivo |
|---|---|---|
| `start` | Sprint 0 | Estrutura inicial, docs, README, `.gitignore`, POM, compose e CI |
| `feature/sprint-1-clientes-veiculos` | Sprint 1 | Clientes e veículos |
| `feature/sprint-2-pecas-servicos-batch` | Sprint 2 | Peças, serviços e batch interno |
| `feature/sprint-3-seed-docs-openapi` | Sprint 3 | Seed, OpenAPI, Postman e docs |
| `feature/sprint-4-integracao-entrega` | Sprint 4 | Integração, hardening e release |
| `test/sprint-5-testes-e-postman` | Sprint 5 | Migração/organização dos testes e Postman |

Regras:

- **Nunca** push direto em `main`.
- Cada sprint = uma branch + PR para `main`.
- Exceção única: commit inicial vazio em `main` para viabilizar o primeiro PR.
- Merge somente com CI verde.

Plano detalhado em [`docs/02-plano-de-tarefas.md`](docs/02-plano-de-tarefas.md).

---

## 12. Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/00-escopo.md`](docs/00-escopo.md) | Escopo e limites do serviço |
| [`docs/01-arquitetura.md`](docs/01-arquitetura.md) | Clean Architecture, módulos e dependências |
| [`docs/02-plano-de-tarefas.md`](docs/02-plano-de-tarefas.md) | Sprints, branches, tarefas e critérios de aceite |
| [`docs/03-modelo-de-dados.md`](docs/03-modelo-de-dados.md) | Tabelas, migrations e seed |
| [`docs/04-contratos-api.md`](docs/04-contratos-api.md) | Endpoints públicos e internos |
| [`docs/05-testes-e-postman.md`](docs/05-testes-e-postman.md) | Migração de testes e Postman |
| [`docs/06-operacao-local.md`](docs/06-operacao-local.md) | Docker, DBeaver, variáveis e troubleshooting |
| [`docs/07-decisoes-e-riscos.md`](docs/07-decisoes-e-riscos.md) | ADRs, decisões e riscos |
| [`docs/adr/ADR-001-stack.md`](docs/adr/ADR-001-stack.md) | Stack Java 17 + Quarkus + Maven multi-módulo |
| [`docs/adr/ADR-002-postgresql.md`](docs/adr/ADR-002-postgresql.md) | PostgreSQL 16 + Flyway |
| [`docs/adr/ADR-003-catalogo-de-pecas.md`](docs/adr/ADR-003-catalogo-de-pecas.md) | Catálogo no Registry × estoque no Supplies |
| [`docs/adr/ADR-004-comunicacao-rest.md`](docs/adr/ADR-004-comunicacao-rest.md) | REST/JSON, RFC 7807 e paginação |
| [`docs/adr/ADR-005-migrations-finais.md`](docs/adr/ADR-005-migrations-finais.md) | Migrations finais e seed |

---

## 13. Licença

Projeto acadêmico — FIAP. Uso educacional.
