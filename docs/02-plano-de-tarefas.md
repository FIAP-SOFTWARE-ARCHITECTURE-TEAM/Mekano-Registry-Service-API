# Plano de tarefas — Mekano Registry Service API

**Fase 4** · **API prefix:** `/api/v4` · **Banco:** `mekano_registry` · **Porta:** `8081`

---

## 1. Estratégia de branches

Cada sprint é entregue em **uma branch por repositório**, com **PR obrigatório** para `main`. Nunca fazer push direto em `main`.

| Branch | Sprint | Objetivo |
|---|---|---|
| `start` | Sprint 0 | Estrutura inicial, docs, README, `.gitignore`, POM, compose e CI |
| `feature/sprint-1-clientes-veiculos` | Sprint 1 | CRUD de clientes e veículos |
| `feature/sprint-2-pecas-servicos-batch` | Sprint 2 | CRUD de peças (catálogo), serviços e batch interno |
| `feature/sprint-3-seed-docs-openapi` | Sprint 3 | Seed, OpenAPI, Postman base e documentação |
| `feature/sprint-4-integracao-entrega` | Sprint 4 | Integração, hardening e release |
| `test/sprint-5-testes-e-postman` | Sprint 5 | Migração/organização final dos testes e Postman |

Regras:

- Exceção única de commit direto em `main`: commit inicial vazio para viabilizar o primeiro PR.
- Merge somente com CI verde.
- A branch `start` é a base do Sprint 0 e já contém README, `.gitignore` e `docs/`.
- A branch `test/sprint-5-testes-e-postman` é criada **depois** do Sprint 4, quando toda a implementação estiver concluída.

---

## 2. Sprint 0 — Fundação (`start`)

### Objetivo

Criar a base do projeto, a documentação e o ambiente local.

### Tarefas

- [x] Criar commit inicial vazio em `main`.
- [x] Criar branch `start`.
- [x] Criar `README.md`.
- [x] Criar `.gitignore`.
- [x] Criar `docs/` com escopo, arquitetura, plano, modelo, contratos, testes, operação, decisões e ADR.
- [x] Criar POM pai e módulos:
  - `registry-domain`
  - `registry-application`
  - `registry-infrastructure`
  - `registry-rest`
- [x] Configurar Java 17, Quarkus 3.36.0, Lombok, MapStruct, Jandex, Surefire e JaCoCo.
- [x] Configurar `quarkus.rest.path=/api/v4`.
- [x] Configurar datasource PostgreSQL, Flyway, OpenAPI, health, metrics e logs.
- [x] Criar `docker-compose.yml` com `registry-db` e `registry-api`.
- [x] Criar `.env.example`.
- [x] Criar `Dockerfile`.
- [x] Criar CI `.github/workflows/ci.yml` com `mvn -B verify`.
- [x] Criar ADRs:
  - `ADR-001-stack.md`
  - `ADR-002-postgresql.md`
  - `ADR-003-catalogo-de-pecas.md`
  - `ADR-004-comunicacao-rest.md`
  - `ADR-005-migrations-finais.md`
- [x] Abrir PR `start → main`.

### Critérios de aceite

- [x] `mvn -B verify` executa sem erros (mesmo sem domínio implementado).
- [x] `docker compose config` válido.
- [x] API sobe em `http://localhost:8081/api/v4`.
- [x] `/q/health` responde `UP`.
- [x] README e `docs/` revisados.
- [x] Nenhum push direto em `main`.

---

## 3. Sprint 1 — Clientes e veículos (`feature/sprint-1-clientes-veiculos`)

### Objetivo

Implementar os CRUDs de clientes e veículos, com validações e migrations finais.

### Tarefas

#### Clientes

- [x] Domínio `Cliente` + VOs `Cpf`, `Email`, `Telefone`, `Endereco`.
- [x] `ClienteService` + commands + ports.
- [x] `ClienteEntity`, repositório Panache, mapper.
- [x] Migration `V1__create_clientes.sql`.
- [x] `ClienteResource` + DTOs + paginação + filtros.
- [x] Validação de CPF, e-mail, telefone e CEP.
- [x] Soft delete/reativação.
- [x] `@RolesAllowed` comentado:
  - leitura/escrita: `admin`, `atendente`;
  - exclusão/reativação: `admin`.

#### Veículos

- [x] Domínio `Veiculo` + `PlacaVeiculo`.
- [x] `VeiculoService` + commands + ports.
- [x] `VeiculoEntity`, repositório Panache, mapper.
- [x] Migration `V2__create_veiculos.sql` com FK para `clientes(uuid)`.
- [x] `VeiculoResource` + DTOs + filtros por cliente/placa.
- [x] Validação de cliente ativo.
- [x] `@RolesAllowed` comentado:
  - leitura/escrita: `admin`, `atendente`;
  - exclusão/reativação: `admin`.

#### Testes mínimos e documentação

- [x] Testes unitários dos VOs e serviços.
- [x] Teste de integração dos repositórios.
- [x] Testes REST dos resources.
- [x] Atualizar `docs/03-modelo-de-dados.md` e `docs/04-contratos-api.md`.
- [ ] Abrir PR.

### Critérios de aceite

- [x] CRUD de clientes e veículos em `/api/v4`.
- [x] Validações retornando `400`/`404`/`409` conforme o caso.
- [x] Paginação e filtros funcionando.
- [ ] Migrations aplicadas do zero (validar com PostgreSQL/Docker no PR; testes usam H2).
- [ ] CI verde (build local `mvn -B verify` verde; validar no PR).
- [x] Sem campos de estoque em qualquer tabela.

---

## 4. Sprint 2 — Peças, serviços e batch (`feature/sprint-2-pecas-servicos-batch`)

### Objetivo

Implementar o catálogo de peças, os serviços e o endpoint interno de batch consumido pelo Supplies.

### Tarefas

#### Peças (catálogo)

- [x] Domínio `Peca` com `codigo`, `descricao`, `valorUnitario`, `isActive`.
- [x] **Sem** `saldoAtual`, `saldoReservado` ou `estoqueMinimo`.
- [x] `PecaService` + commands + ports.
- [x] `PecaEntity`, repositório Panache, mapper.
- [x] Migration `V3__create_pecas.sql`.
- [x] `PecaResource` + DTOs + filtros por código/descrição/ativo.
- [x] `@RolesAllowed("admin")` comentado.

#### Serviços

- [x] Domínio `Servico`.
- [x] `ServicoService` + commands + ports.
- [x] `ServicoEntity`, repositório Panache, mapper.
- [x] Migration `V4__create_servicos.sql`.
- [x] `ServicoResource` + DTOs + filtros.
- [x] `@RolesAllowed("admin")` comentado.

#### Batch interno

- [x] `PecaBatchResource` em `/api/v4/internal/pecas/batch`.
- [x] Request `{ "pecaUuids": [...] }`.
- [x] Response `{ "pecas": [...], "naoEncontradas": [...] }`.
- [x] Limite de lote de 100 UUIDs e validação de lista vazia.
- [x] Retornar peças inativas com `isActive=false`.
- [x] `@RolesAllowed("service:supplies-purchase")` comentado.
- [x] Testes de contrato do batch (request/response e validações).

#### Testes mínimos e documentação

- [x] Testes unitários de `Peca`/`Servico`.
- [x] Testes de integração dos repositórios.
- [x] Testes REST dos resources e do batch.
- [x] Atualizar `docs/03-modelo-de-dados.md`, `docs/04-contratos-api.md` e `docs/01-arquitetura.md`.
- [ ] Abrir PR.

### Critérios de aceite

- [x] CRUD de peças e serviços em `/api/v4`.
- [x] Nenhum campo de estoque na tabela `pecas`.
- [x] Batch funcionando e documentado.
- [x] Contrato do batch compatível com o cliente do Supplies.
- [x] CI verde (`mvn -B verify -pl registry-rest -am`).

---

## 5. Sprint 3 — Seed, OpenAPI e documentação (`feature/sprint-3-seed-docs-openapi`)

### Objetivo

Fechar os dados de demonstração, a documentação OpenAPI/Postman e a documentação final do serviço.

### Tarefas

- [x] Migration `V5__seed_dados_iniciais.sql`:
  - 1 cliente;
  - 1 veículo;
  - 2 serviços;
  - 2 peças (catálogo).
- [x] UUIDs fixos documentados.
- [x] `ON CONFLICT DO NOTHING` para tolerar reexecução local.
- [x] Teste de integração validando o seed.
- [x] OpenAPI final com todos os endpoints e schemas.
- [x] Postman base `docs/postman/Mekano-Registry.postman_collection.json` com:
  - base URL `http://localhost:8081/api/v4`;
  - variáveis de ambiente;
  - UUIDs do seed.
- [x] README final: setup, DBeaver, endpoints, roles, testes.
- [x] `docs/06-operacao-local.md` revisado.
- [x] Hardening: erros, validação, logs, health, metrics, paginação.
- [ ] Abrir PR.

### Critérios de aceite

- [x] Migrations V1–V5 aplicam schema + seed em PostgreSQL 16 descartável.
- [x] Seed validado no PostgreSQL e no teste de integração.
- [x] Swagger/OpenAPI validado com paths e schemas esperados.
- [ ] Postman base executável contra uma instância ativa.
- [x] README e docs revisados.
- [x] CI verde (`mvnw -B verify -pl registry-rest -am`).

---

## 6. Sprint 4 — Integração e entrega (`feature/sprint-4-integracao-entrega`)

### Objetivo

Validar o contrato com o Supplies, fazer hardening e preparar a release.

### Tarefas

- [x] Contract tests do batch com o cliente do Supplies (WireMock).
- [x] E2E com os dois serviços em Compose isolado:
  - Registry com seed;
  - Supplies criando requisição com peça do seed;
  - validação do batch;
  - respostas de peça inexistente/inativa.
- [x] Hardening de erros, validação do contrato e timeout do cliente.
- [x] Auditoria de roles comentadas em todos os endpoints.
- [x] README e docs finais.
- [x] Diagramas Mermaid (arquitetura e fluxo de batch).
- [ ] Tag `v4.0.0` (criar após merge do PR final).
- [ ] PR final.

### Critérios de aceite

- [x] `mvn -B verify -pl registry-rest -am` e `mvn -B verify -pl supplies-rest -am` verdes.
- [x] Compose sobe os dois serviços.
- [x] E2E do batch verde.
- [x] Documentação completa.
- [x] Nenhum endpoint sem `@RolesAllowed` comentado.
- [ ] Release tagueada.

---

## 7. Sprint 5 — Testes e Postman (`test/sprint-5-testes-e-postman`)

> **Esta branch é criada após toda a implementação (Sprints 1–4).**
> O objetivo é migrar, organizar e estabilizar os testes automatizados e os JSONs do Postman.

### Objetivo

Concentrar a migração dos testes do monólito e a organização final dos artefatos de teste, sem adicionar novas funcionalidades.

### Tarefas

- [ ] Migrar testes de domínio de `../Mekano-API/mekano-domain/src/test`:
  - `ClienteTest`, `VeiculoTest`, `PecaTest` (somente catálogo), `ServicoTest`;
  - `CpfTest`, `EmailTest`, `EnderecoTest`, `PlacaVeiculoTest`, `TelefoneTest`;
  - `DomainRecordsTest`.
- [ ] Migrar testes de application de `../Mekano-API/mekano-application/src/test`:
  - `ClienteServiceTest`, `VeiculoServiceTest`, `PecaServiceTest` (catálogo), `ServicoServiceTest`.
- [ ] Migrar testes de infrastructure de `../Mekano-API/mekano-infrastructure/src/test`:
  - repositórios de cliente, veículo, peça, serviço;
  - mappers e auditoria.
- [ ] Migrar testes REST de `../Mekano-API/mekano-rest/src/test`:
  - `ClienteResourceTest`, `VeiculoResourceTest`, `VeiculoGetResourceTest`;
  - `PecaResourceTest`, `ServicoResourceTest`;
  - testes de fault tolerance aplicáveis.
- [ ] **Não migrar** testes de estoque, requisição, NF, OS, orçamento, pagamento, WhatsApp e autenticação.
- [ ] Adaptar pacotes para `com.fiap.mekano.registry.*`.
- [ ] Adaptar paths para `/api/v4`.
- [ ] Migrar o JSON do Postman de `../Mekano-API/newman/Mekano_API_V2.0.postman_collection.json` para `docs/postman/Mekano-Registry.postman_collection.json`.
- [ ] Separar apenas as requisições do Registry; remover as de outros domínios.
- [ ] Atualizar base URL para `http://localhost:8081/api/v4`.
- [ ] Atualizar variáveis e UUIDs do seed.
- [ ] Organizar os testes por módulo e por tipo.
- [ ] Atualizar CI para rodar a suíte completa.
- [ ] Configurar cobertura JaCoCo.
- [ ] Atualizar `docs/05-testes-e-postman.md`.
- [ ] Abrir PR.

### Critérios de aceite

- [ ] Suíte completa verde no CI.
- [ ] Nenhum teste de domínio fora do escopo do Registry.
- [ ] Postman do Registry executável com o seed.
- [ ] Documentação de testes e Postman atualizada.
- [ ] Branch `test/sprint-5-testes-e-postman` mergeada após o Sprint 4.

---

## 8. Ordem de merge

```text
Sprint 0 (start)
   ↓
Sprint 1 (clientes/veículos)
   ↓
Sprint 2 (peças/serviços/batch)
   ↓
Sprint 3 (seed/docs/OpenAPI)
   ↓
Sprint 4 (integração/release)
   ↓
Sprint 5 (testes/Postman) — branch final
```

O Sprint 2 do Registry deve ser mergeado **antes** do Sprint 2 do Supplies, pois o Supplies consome o batch de peças.

---

## 9. Definition of Done (por sprint)

- [ ] Código compilando e testes verdes.
- [ ] Migrations aplicadas do zero.
- [ ] Documentação atualizada em `docs/`.
- [ ] README atualizado quando necessário.
- [ ] `@RolesAllowed` comentado com `TODO` nos novos endpoints.
- [ ] Sem push direto em `main`.
- [ ] PR revisado e mergeado.
- [ ] Nenhum acesso ao banco do Supplies.
- [ ] Prefixo `/api/v4` respeitado.
