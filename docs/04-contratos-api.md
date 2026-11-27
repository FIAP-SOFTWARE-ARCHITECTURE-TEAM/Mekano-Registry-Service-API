# Contratos de API — Mekano Registry Service API

**Prefixo:** `/api/v4`
**Base local:** `http://localhost:8081/api/v4`

> **Status:** contratos de clientes, veículos, catálogo, serviços e batch documentados para os Sprints 1–3. O documento OpenAPI gerado pela aplicação é a referência verificável dos schemas e respostas.

---

## 1. Convenções

- JSON em UTF-8.
- Datas em ISO-8601 (`2026-11-26T10:00:00`).
- Valores monetários como número decimal (`45.90`).
- Paginação:
  - query `page` (default `0`) e `size` (default `20`, máximo `100`);
  - query `sort` no formato `campo,asc|desc` (ex.: `nome,asc`, `placa,asc`);
  - response com `content`, `page`, `size`, `totalElements`, `totalPages`.
- Erros no padrão **RFC 7807** (`application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Cliente não encontrado: 11111111-1111-1111-1111-111111111111",
  "instance": "/api/v4/clientes/11111111-1111-1111-1111-111111111111"
}
```

- `@RolesAllowed` comentado com `TODO` em todos os endpoints.

---

## 2. Clientes

### 2.1 Criar

```http
POST /api/v4/clientes
Content-Type: application/json

{
  "nome": "Cliente Exemplo",
  "cpf": "52998224725",
  "email": "cliente.exemplo@mekano.com.br",
  "telefone": "11999999999",
  "endereco": {
    "logradouro": "Rua Exemplo",
    "numero": "100",
    "complemento": null,
    "bairro": "Centro",
    "cidade": "São Paulo",
    "uf": "SP",
    "cep": "01001000"
  }
}
```

Resposta `201`:

```json
{
  "uuid": "11111111-1111-1111-1111-111111111111",
  "nome": "Cliente Exemplo",
  "cpf": "52998224725",
  "email": "cliente.exemplo@mekano.com.br",
  "telefone": "11999999999",
  "endereco": {
    "logradouro": "Rua Exemplo",
    "numero": "100",
    "complemento": null,
    "bairro": "Centro",
    "cidade": "São Paulo",
    "uf": "SP",
    "cep": "01001000"
  },
  "isActive": true,
  "createdAt": "2026-11-26T10:00:00"
}
```

### 2.2 Listar

```http
GET /api/v4/clientes?page=0&size=20&sort=nome,asc&nome=Cliente&isActive=true
```

Filtros: `nome` (contém, case-insensitive) e `isActive`.

### 2.3 Buscar por UUID

```http
GET /api/v4/clientes/{uuid}
```

### 2.4 Atualizar

```http
PUT /api/v4/clientes/{uuid}
```

> CPF não é alterável após a criação.

### 2.5 Desativar / reativar

```http
DELETE /api/v4/clientes/{uuid}
PATCH  /api/v4/clientes/{uuid}/reativar
```

- `DELETE` = soft delete (`isActive=false`, `deletedAt`).
- `PATCH /reativar` = `isActive=true`.

### 2.6 Roles comentadas

```java
// TODO: habilitar quando autenticação entrar no escopo
// @RolesAllowed({"admin", "atendente"})
```

Exclusão/reativação: `@RolesAllowed("admin")`.

---

## 3. Veículos

### 3.1 Criar

```http
POST /api/v4/veiculos
Content-Type: application/json

{
  "clienteUuid": "11111111-1111-1111-1111-111111111111",
  "placa": "ABC1D23",
  "marca": "Volkswagen",
  "modelo": "Gol",
  "ano": 2020
}
```

Resposta `201`:

```json
{
  "uuid": "22222222-2222-2222-2222-222222222222",
  "clienteUuid": "11111111-1111-1111-1111-111111111111",
  "placa": "ABC1D23",
  "marca": "Volkswagen",
  "modelo": "Gol",
  "ano": 2020,
  "isActive": true,
  "createdAt": "2026-11-26T10:00:00"
}
```

### 3.2 Listar / buscar / atualizar / desativar

```http
GET    /api/v4/veiculos?page=0&size=20&sort=placa,asc&clienteUuid=...&placa=ABC1D23&isActive=true
GET    /api/v4/veiculos/{uuid}
PUT    /api/v4/veiculos/{uuid}
DELETE /api/v4/veiculos/{uuid}
PATCH  /api/v4/veiculos/{uuid}/reativar
```

Filtros: `clienteUuid`, `placa` (contém, case-insensitive) e `isActive`.

Regras:

- Cliente precisa existir e estar ativo.
- Placa única entre veículos ativos.
- Placa aceita padrão antigo (`ABC1234`) e Mercosul (`ABC1D23`).
- Placa e cliente são imutáveis após a criação; `PUT` altera apenas marca, modelo e ano.
- `DELETE` executa soft delete; `PATCH /reativar` reativa.
- Erros: `400` (validação), `404` (cliente/veículo inexistente) e `409` (CPF/placa duplicado, cliente inativo).

---

## 4. Peças (catálogo)

> **Sem estoque.** Saldo, reservado e mínimo pertencem ao Supplies.

### 4.1 Criar

```http
POST /api/v4/pecas
Content-Type: application/json

{
  "codigo": "PEA-001",
  "descricao": "Filtro de Óleo",
  "valorUnitario": 45.90
}
```

Resposta `201`:

```json
{
  "uuid": "55555555-5555-5555-5555-555555555555",
  "codigo": "PEA-001",
  "descricao": "Filtro de Óleo",
  "valorUnitario": 45.90,
  "isActive": true,
  "createdAt": "2026-11-26T10:00:00"
}
```

### 4.2 Listar / buscar / atualizar / desativar

```http
GET    /api/v4/pecas?page=0&size=20&codigo=PEA&descricao=Filtro&isActive=true
GET    /api/v4/pecas/{uuid}
PUT    /api/v4/pecas/{uuid}
DELETE /api/v4/pecas/{uuid}
PATCH  /api/v4/pecas/{uuid}/reativar
```

Regras:

- Código único entre peças ativas.
- Valor unitário > 0.
- Não há endpoint de saldo/estoque no Registry.
- `PUT` altera código, descrição e valor unitário; `DELETE` é soft delete e `PATCH /reativar` pode retornar `409` se o código já estiver em uso.
- Resposta nunca inclui campos de estoque.

---

## 5. Serviços

### 5.1 Criar

```http
POST /api/v4/servicos
Content-Type: application/json

{
  "nome": "Troca de Óleo",
  "descricao": "Troca de óleo e filtro",
  "valorUnitario": 150.00
}
```

Resposta `201` contém `uuid`, `nome`, `descricao`, `valorUnitario`, `isActive` e `createdAt`.

### 5.2 Listar / buscar / atualizar / desativar

```http
GET    /api/v4/servicos?page=0&size=20&nome=Troca&isActive=true
GET    /api/v4/servicos/{uuid}
PUT    /api/v4/servicos/{uuid}
DELETE /api/v4/servicos/{uuid}
PATCH  /api/v4/servicos/{uuid}/reativar
```

Regras:

- Nome único entre serviços ativos.
- Valor unitário > 0.
- `PUT` atualiza nome, descrição e valor unitário; `DELETE` é soft delete e `PATCH /reativar` pode retornar `409` se o nome já estiver em uso.

---

## 6. Endpoint interno — batch de peças

### 6.1 Request

```http
POST /api/v4/internal/pecas/batch
Content-Type: application/json

{
  "pecaUuids": [
    "55555555-5555-5555-5555-555555555555",
    "66666666-6666-6666-6666-666666666666",
    "99999999-9999-9999-9999-999999999999"
  ]
}
```

### 6.2 Response `200`

```json
{
  "pecas": [
    {
      "uuid": "55555555-5555-5555-5555-555555555555",
      "codigo": "PEA-001",
      "descricao": "Filtro de Óleo",
      "valorUnitario": 45.90,
      "isActive": true
    },
    {
      "uuid": "66666666-6666-6666-6666-666666666666",
      "codigo": "PEA-002",
      "descricao": "Pastilha de Freio",
      "valorUnitario": 120.00,
      "isActive": true
    }
  ],
  "naoEncontradas": [
    "99999999-9999-9999-9999-999999999999"
  ]
}
```

### 6.3 Regras

- Lista vazia → `400`.
- Mais de 100 UUIDs → `400`.
- Peças inativas retornam `isActive=false`.
- Peças inexistentes retornam em `naoEncontradas`.
- UUIDs repetidos na entrada são processados uma única vez, mantendo a ordem da primeira ocorrência.
- `@RolesAllowed("service:supplies-purchase")` comentado com `TODO`.

O formato de `pecas[]` (`uuid`, `codigo`, `descricao`, `valorUnitario`, `isActive`) é o consumido pelo cliente REST do Supplies.

---

## 7. Códigos de erro

| Status | Quando |
|---|---|
| `400` | Validação de entrada, CPF/placa/e-mail inválido, lote vazio/grande |
| `404` | Recurso não encontrado |
| `409` | Duplicidade (CPF, placa, código, nome) ou conflito de estado |
| `422` | Regra de negócio não atendida (quando aplicável) |
| `500` | Erro inesperado |

---

## 8. OpenAPI

- Swagger UI: `http://localhost:8081/q/swagger-ui`
- OpenAPI JSON: `http://localhost:8081/q/openapi`

O documento gerado descreve os paths sob `/api/v4`, os DTOs de request/response e as respostas de erro RFC 7807. A descrição também inclui o endpoint interno de batch de peças. Execute `mvn -B verify -pl registry-rest -am` para validar a presença dos paths e schemas esperados no OpenAPI.

## 9. Integração com Supplies

O Supplies chama `POST /api/v4/internal/pecas/batch` ao criar uma requisição. O batch deve retornar exatamente uma classificação para cada UUID solicitado: o objeto da peça em `pecas` (inclusive quando inativa) ou o UUID em `naoEncontradas`.

- UUID inexistente → o Supplies responde `404`.
- Peça encontrada com `isActive=false` → o Supplies responde `409`.
- Timeout, falha HTTP ou resposta incompleta/inválida do Registry → o Supplies responde `503`.

O cliente usa timeout configurável (`REGISTRY_TIMEOUT_MS`, padrão de 3000 ms). Os cenários reais de integração são exercitados por `scripts/Test-RegistrySuppliesE2E.ps1`.
