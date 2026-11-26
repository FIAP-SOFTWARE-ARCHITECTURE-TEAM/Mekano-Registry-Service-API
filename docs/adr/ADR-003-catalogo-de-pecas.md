# ADR-003 — Catálogo de peças no Registry, estoque no Supplies

**Status:** Aceita
**Fase:** 4
**Data:** 2026-11-26

---

## 1. Contexto

No monólito Mekano, a entidade `Peca` acumulava duas responsabilidades:

1. **Catálogo:** código, descrição e valor unitário.
2. **Estoque:** saldo atual, saldo reservado, estoque mínimo e movimentos.

Na nova arquitetura de microsserviços, o Registry foi definido como o serviço de **cadastros básicos** (clientes, veículos, peças e serviços) e o Supplies como o serviço de **requisição de compra e NF de entrada**.

O ponto crítico é que a NF de entrada precisa **creditar estoque**. Se o estoque ficasse no Registry, o registro da NF no Supplies exigiria uma chamada remota e um mecanismo de idempotência + flag + reprocessamento para lidar com falhas parciais entre dois bancos.

Além disso, o estoque não é um simples campo de cadastro: ele é um **ledger transacional** com reservas, liberações, consumo e movimentos. No monólito, ele é alterado por diversos fluxos (reposição, compras, NF e consumo), o que reforça que se trata de um bounded context próprio.

---

## 2. Decisão

**O Registry possui apenas o catálogo de peças. O estoque pertence ao Supplies.**

### Registry

- Tabela `pecas` com:
  - `uuid`
  - `codigo`
  - `descricao`
  - `valor_unitario`
  - `is_active`
  - auditoria
- **Sem** `saldo_atual`, `saldo_reservado` ou `estoque_minimo`.
- Endpoint interno `POST /api/v4/internal/pecas/batch` para consulta em lote.

### Supplies

- Tabela `estoque` com:
  - `peca_uuid` (referência lógica)
  - `saldo_atual`
  - `saldo_reservado`
  - `estoque_minimo`
  - `version`
- Tabela `movimentos_estoque` com ledger e `idempotency_key` única.
- API interna de estoque em `/api/v4/internal/estoque/**`.
- NF de entrada e cancelamento em **transação local ACID**.

---

## 3. Consequências

### Positivas

1. **NF + estoque em uma única transação local**: salvar NF, creditar estoque e gravar movimentos ocorrem no mesmo banco. Falha = rollback total.
2. **Cancelamento de NF com estorno local**: sem compensação remota.
3. **Reposição e requisição de compra no mesmo serviço**: estoque mínimo, movimentos e requisição ficam coesos.
4. **Registry permanece um CRUD de catálogo**, sem acoplamento com compras.
5. **Sem ciclo de dependência**: Supplies → Registry (batch); Registry não chama Supplies.
6. **Ledger de estoque, NF e requisição no mesmo serviço**, facilitando auditoria.

### Negativas / custos

1. **Agregado “peça” dividido** entre catálogo (Registry) e estoque (Supplies).
2. **Referência lógica** por `peca_uuid`, sem FK cross-database.
3. **Criação sob demanda** da linha de estoque (`obterOuCriar`).
4. **Seed dividido**: catálogo no Registry, estoque inicial no Supplies.
5. **Necessidade de validação de peça** no Supplies via batch do Registry.

### Mitigações

- `obterOuCriar` cria a linha de estoque com saldo 0 na primeira referência.
- Snapshots de código/descrição/valor no item da requisição.
- Batch retorna `isActive`; o Supplies decide bloquear novas operações para peças inativas.
- UUIDs fixos no seed e testes de contrato/E2E.
- Documentação explícita em ambos os repositórios.

---

## 4. Alternativas consideradas

### Alternativa A — Estoque no Registry (rejeitada)

- Registry teria `saldo_atual`, `saldo_reservado`, `estoque_minimo`.
- Supplies chamaria o Registry para creditar/estornar estoque na NF.
- Exigiria idempotência + flag `estoque_creditado` + endpoint de reprocessamento.
- **Motivo da rejeição:** transação distribuída, maior complexidade e risco de inconsistência parcial.

### Alternativa B — Estoque no Supplies (aceita)

- NF e estoque no mesmo banco.
- Transação local ACID.
- Idempotência mantida apenas no ledger para chamadas remotas futuras.
- **Motivo da escolha:** simplicidade, consistência e melhor aderência ao domínio de suprimentos.

### Alternativa C — Serviço dedicado de estoque (futuro)

- Poderia ser extraído depois, se o volume ou o domínio justificarem.
- A API interna de estoque já isola o contrato, facilitando uma futura extração.

---

## 5. Impacto nos contratos

### Registry

- `POST /api/v4/internal/pecas/batch`
- Sem endpoints de estoque.

### Supplies

- `POST /api/v4/internal/estoque/movimentos`
- `GET /api/v4/internal/estoque/{pecaUuid}`
- `GET /api/v4/internal/estoque/abaixo-minimo`
- NF local: `POST /api/v4/nf-entrada` e `POST /api/v4/nf-entrada/{uuid}/cancelar`

---

## 6. Migração de dados

Não há migração de dados:

- Projeto acadêmico reiniciado a cada fase.
- As migrations são escritas já no estado final.
- O seed do Registry cria o catálogo; o seed do Supplies cria o estoque inicial das mesmas peças.

---

## 7. Referências

- [`../00-escopo.md`](../00-escopo.md)
- [`../03-modelo-de-dados.md`](../03-modelo-de-dados.md)
- [`../04-contratos-api.md`](../04-contratos-api.md)
- Documentação do Supplies: `Mekano-Supplies-Purchase-Request-Service-API/docs/adr/ADR-003-ownership-estoque.md`
