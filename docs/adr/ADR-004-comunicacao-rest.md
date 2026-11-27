# ADR-004 — Comunicação REST/JSON síncrona

**Status:** Aceita
**Fase:** 4
**Data:** 2026-11-26

---

## 1. Contexto

O Registry expõe cadastros e catálogo para consumo pelo monólito Mekano, pelo serviço de Supplies e por ferramentas de demonstração (Postman/Newman).

Os consumidores precisam de:

- contrato simples e estável;
- operações CRUD com paginação e filtros;
- erros padronizados e previsíveis;
- versionamento explícito;
- baixo acoplamento (sem broker nesta fase).

---

## 2. Decisão

Adotar **REST/JSON síncrono** sobre HTTP, com prefixo `/api/v4`.

### Convenções

| Item | Decisão |
|---|---|
| Prefixo | `/api/v4` via `quarkus.rest.path` |
| Porta local | `8081` |
| Formato | JSON UTF-8 |
| Datas | ISO-8601 (`2026-11-26T10:00:00`) |
| Valores monetários | Número decimal (`45.90`) |
| Paginação | `page` (default `0`), `size` (default `20`, máximo `100`) |
| Resposta paginada | `content`, `page`, `size`, `totalElements`, `totalPages` |
| Erros | RFC 7807 (`application/problem+json`) |
| DTOs de entrada | Classes com Bean Validation |
| DTOs de saída | `record` imutáveis |
| Mapeamento | MapStruct (`componentModel = "cdi"`) |
| Segurança | `@RolesAllowed` comentado com `TODO` (auth fora do escopo) |

### Endpoints implementados no Sprint 1

```http
POST   /api/v4/clientes
GET    /api/v4/clientes?page=0&size=20&nome=...&isActive=true
GET    /api/v4/clientes/{uuid}
PUT    /api/v4/clientes/{uuid}
DELETE /api/v4/clientes/{uuid}
PATCH  /api/v4/clientes/{uuid}/reativar

POST   /api/v4/veiculos
GET    /api/v4/veiculos?page=0&size=20&clienteUuid=...&placa=...&isActive=true
GET    /api/v4/veiculos/{uuid}
PUT    /api/v4/veiculos/{uuid}
DELETE /api/v4/veiculos/{uuid}
PATCH  /api/v4/veiculos/{uuid}/reativar
```

### Tratamento de erro

Um único `ApiExceptionMapper` converte:

- `AppException` → status HTTP definido no domínio (`400`, `404`, `409`);
- `ConstraintViolationException` / `ValidationException` → `400`;
- `WebApplicationException` → status original;
- exceções inesperadas → `500`.

Formato:

```json
{
  "type": "about:blank",
  "title": "Recurso nao encontrado",
  "status": 404,
  "detail": "Cliente nao encontrado: 11111111-1111-1111-1111-111111111111",
  "instance": "/api/v4/clientes/11111111-1111-1111-1111-111111111111"
}
```

---

## 3. Consequências

### Positivas

1. **Contrato simples** e amplamente suportado por clientes e ferramentas.
2. **Versionamento explícito** no path (`/api/v4`).
3. **Erros uniformes** no padrão RFC 7807.
4. **Paginação e filtros previsíveis** para listagens.
5. **Baixo acoplamento**: o Registry não conhece os consumidores.
6. **Evolução incremental** por sprint, com OpenAPI gerado pelo Quarkus.

### Negativas / custos

1. **Chamadas síncronas** podem sofrer com latência/indisponibilidade do consumidor.
2. **Sem eventos**: operações que precisem de reação assíncrona ficam fora desta fase.
3. **Versionamento manual** do path exige disciplina em novas versões.
4. **Auth fora do escopo**: endpoints ficam públicos até a sprint de autenticação.

### Mitigações

- Timeout e tratamento de erro no cliente (Supplies/monólito).
- OpenAPI como fonte de verdade do contrato.
- `@RolesAllowed` comentado em todos os endpoints para ativação futura.
- Testes REST cobrindo contratos, validações e códigos de erro.

---

## 4. Alternativas consideradas

### Alternativa A — gRPC (rejeitada)

- Melhor desempenho e contrato binário.
- Exigiria toolchain e clientes adicionais; pouco aderente ao ecossistema atual.
- **Motivo da rejeição:** complexidade desnecessária para CRUDs de cadastro.

### Alternativa B — GraphQL (rejeitada)

- Flexível para consultas, mas adiciona complexidade de schema/resolvers.
- Não é o padrão do monólito nem do Supplies.
- **Motivo da rejeição:** simplicidade e consistência.

### Alternativa C — Mensageria/eventos desde o início (rejeitada nesta fase)

- Desacoplamento e resiliência.
- Exige broker, idempotência e observabilidade adicionais.
- **Motivo da rejeição:** nenhum consumidor assíncrono existe nesta fase.

### Alternativa D — Prefixo `/api/v1` (rejeitada)

- Convenção comum, mas o projeto está na Fase 4.
- **Motivo da rejeição:** alinhamento com o monólito e com os demais serviços da fase (`/api/v4`).

---

## 5. Referências

- [`../04-contratos-api.md`](../04-contratos-api.md)
- [`../07-decisoes-e-riscos.md`](../07-decisoes-e-riscos.md)
- [`ADR-001-stack.md`](ADR-001-stack.md)
