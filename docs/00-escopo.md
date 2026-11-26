# Escopo — Mekano Registry Service API

## 1. Objetivo

O Registry é o serviço de **dados mestres e catálogo** do Mekano. Ele centraliza o cadastro de clientes, veículos, peças/insumos e serviços, servindo de base para os demais serviços do sistema.

**Fase:** 4
**Prefixo REST:** `/api/v4`
**Banco:** `mekano_registry` (PostgreSQL 16)
**Porta da API:** `8081`
**Porta do banco:** `5433`

---

## 2. Escopo incluído

### 2.1 Clientes

- Criar, listar, buscar, atualizar, desativar e reativar clientes.
- Dados: nome, CPF, e-mail, telefone, endereço.
- Validações de CPF, e-mail, telefone e CEP.
- Soft delete e auditoria.

### 2.2 Veículos

- Criar, listar, buscar, atualizar, desativar e reativar veículos.
- Dados: placa, marca, modelo, ano e vínculo com cliente.
- Validação de placa (padrão antigo e Mercosul).
- Um cliente só pode ser vinculado a veículos ativos.

### 2.3 Peças/insumos (catálogo)

- Criar, listar, buscar, atualizar, desativar e reativar peças.
- Dados: código, descrição e valor unitário.
- **Não inclui** saldo, saldo reservado, estoque mínimo ou movimentos.
- Código único entre peças ativas.

### 2.4 Serviços

- Criar, listar, buscar, atualizar, desativar e reativar serviços.
- Dados: nome, descrição e valor unitário.
- Nome único entre serviços ativos.

### 2.5 Endpoint interno

- `POST /api/v4/internal/pecas/batch`: consulta em lote de peças por UUID.
- Usado pelo `Mekano-Supplies-Purchase-Request-Service-API` para validar e enriquecer requisições de compra.
- Retorna peças encontradas e lista de UUIDs não encontrados.
- Respeita peças inativas (`isActive=false`).

### 2.6 Infraestrutura e entrega

- Migrations Flyway finais.
- Seed de demonstração.
- Docker Compose local com PostgreSQL.
- README, `.gitignore` e documentação em `docs/`.
- CI com `mvn verify`.
- Migração de testes e Postman na branch final.

---

## 3. Fora de escopo

| Item | Onde pertence |
|---|---|
| Estoque (saldo, reservado, mínimo, movimentos) | `Mekano-Supplies-Purchase-Request-Service-API` |
| Requisição de compra | `Mekano-Supplies-Purchase-Request-Service-API` |
| NF de entrada | `Mekano-Supplies-Purchase-Request-Service-API` |
| Ordens de serviço | Outro repositório (fase futura) |
| Orçamentos | Outro repositório (fase futura) |
| Pagamentos | Outro repositório (fase futura) |
| WhatsApp | Outro repositório (fase futura) |
| Autenticação/autorização | Fora do escopo; apenas roles comentadas com `TODO` |

> O Registry **não acessa** o banco do Supplies e **não depende** de nenhum endpoint do Supplies.

---

## 4. Decisão de ownership: catálogo × estoque

No monólito, o saldo ficava na entidade `Peca`. Nesta fase, o escopo foi separado:

- **Registry:** identidade e catálogo da peça (`codigo`, `descricao`, `valorUnitario`).
- **Supplies:** estoque (`saldo_atual`, `saldo_reservado`, `estoque_minimo`) e movimentos.

Motivos principais:

1. O estoque é um **ledger transacional**, não um campo de cadastro.
2. A NF de entrada e o cancelamento de NF precisam de **transação local ACID** no Supplies.
3. A reposição por estoque mínimo e a requisição de compra ficam no mesmo serviço.
4. O Registry permanece um CRUD de catálogo, sem acoplamento com o fluxo de compras.

Consequências:

- A peça é referenciada no Supplies por `peca_uuid` (referência lógica, sem FK cross-database).
- O Supplies cria a linha de estoque sob demanda (`obterOuCriar`).
- O seed do Registry cria apenas o catálogo; o seed do Supplies cria o estoque inicial.
- O endpoint de batch é o único contrato interno necessário no Registry.

Detalhes em [`adr/ADR-003-catalogo-de-pecas.md`](adr/ADR-003-catalogo-de-pecas.md).

---

## 5. Critérios de aceite do serviço

- [ ] CRUD de clientes, veículos, peças e serviços funcionando em `/api/v4`.
- [ ] Peças sem campos de estoque.
- [ ] Batch interno de peças funcionando e testado.
- [ ] Migrations finais aplicadas do zero com `docker compose down -v && docker compose up`.
- [ ] Seed com 1 cliente, 1 veículo, 2 serviços e 2 peças.
- [ ] DBeaver conectando em `localhost:5433/mekano_registry`.
- [ ] README e `docs/` completos.
- [ ] CI verde.
- [ ] Nenhum push direto em `main`.
- [ ] Testes e Postman migrados na branch final `test/sprint-5-testes-e-postman`.

---

## 6. Premissas

- Não há dados a migrar: o projeto é acadêmico e reinicia a cada fase.
- As migrations podem ser escritas já no estado final.
- A autenticação não faz parte desta fase.
- A comunicação com o Supplies é síncrona, REST/JSON, em `/api/v4`.
