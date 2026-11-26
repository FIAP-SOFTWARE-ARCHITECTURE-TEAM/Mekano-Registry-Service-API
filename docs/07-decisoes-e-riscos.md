# Decisões e riscos — Mekano Registry Service API

---

## 1. Decisões (ADRs)

| ADR | Decisão | Motivo |
|---|---|---|
| ADR-001 | Java 17 + Quarkus 3.36.0 + Maven multi-módulo | Mesmo stack do monólito; produtividade e consistência |
| ADR-002 | PostgreSQL 16 | Dados relacionais, constraints, Flyway, DBeaver, equipe |
| ADR-003 | Registry = catálogo; estoque no Supplies | Estoque é ledger transacional; NF precisa de ACID local |
| ADR-004 | REST/JSON síncrono | Simplicidade; sem broker nesta fase |
| ADR-005 | Migrations finais + seed | Não há dados a migrar; projeto acadêmico reiniciado |
| ADR-006 | Soft delete + índices únicos parciais | Permite reativar cadastros sem conflito |
| ADR-007 | Problem Details (RFC 7807) | Padrão único de erro |
| ADR-008 | `/api/v4` | Fase 4 do projeto |

---

## 2. Roles futuras (comentadas)

A autenticação está fora do escopo. Todos os endpoints terão `@RolesAllowed` comentado com `TODO`.

### Clientes e veículos

| Operação | Roles propostas |
|---|---|
| `GET`, `POST`, `PUT` | `admin`, `atendente` |
| `DELETE`, `PATCH /reativar` | `admin` |

### Peças e serviços

| Operação | Roles propostas |
|---|---|
| Todas | `admin` |

### Batch interno

| Operação | Roles propostas |
|---|---|
| `POST /api/v4/internal/pecas/batch` | `service:supplies-purchase` |

Formato:

```java
// TODO: habilitar quando autenticação entrar no escopo
// @RolesAllowed({"admin", "atendente"})
```

---

## 3. Decisão de estoque

O Registry **não possui** saldo, reservado ou estoque mínimo.

- A peça é apenas catálogo.
- O Supplies é o dono do estoque e dos movimentos.
- O Supplies referencia a peça por `peca_uuid`.
- O batch é o único contrato necessário.

Detalhes em [`adr/ADR-003-catalogo-de-pecas.md`](adr/ADR-003-catalogo-de-pecas.md).

---

## 4. Riscos e mitigação

| Risco | Impacto | Mitigação |
|---|---|---|
| Peça inativada enquanto o Supplies ainda referencia | Requisições/NF com peça inativa | Batch retorna `isActive`; Supplies decide bloquear novas operações e preservar histórico |
| Divergência de UUID entre Registry e Supplies | Estoque órfão | UUIDs fixos no seed + testes de contrato/E2E |
| Migration alterada após aplicação | Flyway falha | Reset de volume em dev; migrations finais |
| Contrato do batch mudar | Quebra o Supplies | OpenAPI + contract tests + versionamento `/api/v4` |
| Porta 5433/8081 ocupada | Ambiente não sobe | Variáveis de ambiente configuráveis |
| Roles esquecidas | Endpoint sem proteção futura | Checklist de `@RolesAllowed` comentado em todos os endpoints |
| Escopo crescer | Atraso | Estoque/NF/requisição/OS/orçamento/auth fora do escopo |

---

## 5. Pontos de atenção

- Não criar endpoints de estoque no Registry.
- Não criar FK cross-database.
- Não acessar o banco do Supplies.
- Não commitar `.env` com credenciais.
- Não fazer push direto em `main`.
- Manter `/api/v4` em todos os endpoints, testes e Postman.

---

## 6. ADRs relacionados

- [`adr/ADR-003-catalogo-de-pecas.md`](adr/ADR-003-catalogo-de-pecas.md)
- [`05-testes-e-postman.md`](05-testes-e-postman.md)
- [`03-modelo-de-dados.md`](03-modelo-de-dados.md)
