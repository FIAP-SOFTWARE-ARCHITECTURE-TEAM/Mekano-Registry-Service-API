# Testes e Postman — Mekano Registry Service API

**Branch final:** `test/sprint-5-testes-e-postman`
**Base URL:** `http://localhost:8081/api/v4`

---

## 1. Estratégia

A implementação (Sprints 1–4) mantém o build verde com testes mínimos das novas classes. A **migração completa** dos testes existentes do monólito e a **organização dos JSONs do Postman** são concentradas na branch final `test/sprint-5-testes-e-postman`, após o Sprint 4.

Tipos de teste:

| Tipo | Ferramenta | O que cobre |
|---|---|---|
| Unitário de domínio | JUnit 5 + AssertJ | Entidades, VOs, regras |
| Unitário de application | JUnit 5 + Mockito | Services, validações, ports |
| Integração | QuarkusTest + PostgreSQL (Dev Services/Testcontainers) | Repositórios, migrations, mappers |
| REST | QuarkusTest + RestAssured | Resources, DTOs, status, Problem Details |
| Contrato | WireMock | Cliente do batch consumido pelo Supplies |
| E2E | Postman/Newman | Fluxo com os dois serviços |

---

## 2. Migração dos testes do monólito

Origem: `../Mekano-API/*/src/test`.

### 2.1 Domínio (`mekano-domain/src/test`)

| Teste de origem | Destino no Registry | Observação |
|---|---|---|
| `ClienteTest` | `registry-domain` | Migrar integralmente |
| `VeiculoTest` | `registry-domain` | Migrar integralmente |
| `PecaTest` | `registry-domain` | Migrar **somente catálogo**; remover testes de saldo/reserva |
| `ServicoTest` | `registry-domain` | Migrar integralmente |
| `CpfTest` | `registry-domain` | Migrar integralmente |
| `EmailTest` | `registry-domain` | Migrar integralmente |
| `EnderecoTest` | `registry-domain` | Migrar integralmente |
| `PlacaVeiculoTest` | `registry-domain` | Migrar integralmente |
| `TelefoneTest` | `registry-domain` | Migrar integralmente |
| `DomainRecordsTest` | `registry-domain` | Adaptar aos records do Registry |

**Não migrar:** `NfEntradaTest`, `RequisicaoCompraTest`, `OrcamentoTest`, `OrdemDeServico*`, `ItemOrcamentoTest`, `UserTest`, eventos de OS/pagamento.

### 2.2 Application (`mekano-application/src/test`)

| Teste de origem | Destino no Registry | Observação |
|---|---|---|
| `ClienteServiceTest` | `registry-application` | Migrar integralmente |
| `VeiculoServiceTest` | `registry-application` | Migrar integralmente |
| `PecaServiceTest` | `registry-application` | Migrar somente catálogo |
| `ServicoServiceTest` | `registry-application` | Migrar integralmente |

**Não migrar:** `RequisicaoCompraServiceTest`, `NfEntradaServiceTest`, `OrcamentoServiceTest`, `OrdemDeServicoServiceTest`, `MockPaymentServiceTest`, `WhatsApp*`, `Auth*`, `AdminUserServiceTest`, `OsAudit*`, `CobrancaEmitidaListenerTest`.

### 2.3 Infrastructure (`mekano-infrastructure/src/test`)

| Teste de origem | Destino no Registry | Observação |
|---|---|---|
| `ClienteRepositoryImplTest` | `registry-infrastructure` | Migrar integralmente |
| `VeiculoRepositoryImplTest` | `registry-infrastructure` | Migrar integralmente |
| `PecaRepositoryImplTest` | `registry-infrastructure` | Migrar somente catálogo |
| `ServicoRepositoryImplTest` | `registry-infrastructure` | Migrar integralmente |
| `EnderecoMapperTest` | `registry-infrastructure` | Migrar integralmente |
| `AuditoriaListenerTest` | `registry-infrastructure` | Migrar integralmente |
| `AuditoriaListenerIntegrationTest` | `registry-infrastructure` | Migrar integralmente |
| `AuditoriaOrigemTest` | `registry-infrastructure` | Migrar integralmente |

**Não migrar:** `NfEntradaRepositoryImplTest`, `RequisicaoCompraRepositoryImplTest`, `OrcamentoRepositoryImplTest`, `OrdemDeServicoRepositoryImplTest`, `SlaExpiryJobTest`, `PecaOrcamentoObserverTest`, `EstoqueMinimoObserverTest`, `WhatsApp*`, `Pagamento*`, `OS*`, `RefreshToken*`, `User*`.

### 2.4 REST (`mekano-rest/src/test`)

| Teste de origem | Destino no Registry | Observação |
|---|---|---|
| `ClienteResourceTest` | `registry-rest` | Adaptar path para `/api/v4` |
| `VeiculoResourceTest` | `registry-rest` | Adaptar path para `/api/v4` |
| `VeiculoGetResourceTest` | `registry-rest` | Adaptar path para `/api/v4` |
| `PecaResourceTest` | `registry-rest` | Adaptar path e remover campos de estoque |
| `ServicoResourceTest` | `registry-rest` | Adaptar path para `/api/v4` |
| `FaultToleranceTest` / `VeiculoFaultToleranceTest` | `registry-rest` | Migrar apenas cenários de cliente/veículo/peça/serviço |
| `AuditoriaAuditFieldsTest` | `registry-rest` | Adaptar entidades do Registry |

**Não migrar:** `RequisicaoCompraResourceTest`, `NfEntradaResourceTest`, `AlertaResourceTest`, `Orcamento*`, `OrdemDeServico*`, `Pagamento*`, `AdminUser*`, `WebhookEvolution*`, `LiberadoParaExecucaoTest`, `OsAuditResourceTest`, `ObservabilityEndpointsTest` (recriar se necessário), `ClasspathDiagnosticTest`.

---

## 3. Migração do Postman

Origem: `../Mekano-API/newman/Mekano_API_V2.0.postman_collection.json`.

Destino: `docs/postman/Mekano-Registry.postman_collection.json`.

Passos:

1. Copiar a coleção de origem.
2. Manter apenas as requisições de:
   - clientes;
   - veículos;
   - peças (catálogo);
   - serviços;
   - batch interno de peças.
3. Remover requisições de estoque, requisição de compra, NF, OS, orçamento, pagamento, WhatsApp e auth.
4. Alterar a `baseUrl` para `http://localhost:8081/api/v4`.
5. Atualizar variáveis:
   - `clienteUuid = 11111111-1111-1111-1111-111111111111`
   - `veiculoUuid = 22222222-2222-2222-2222-222222222222`
   - `servico1Uuid = 33333333-3333-3333-3333-333333333333`
   - `servico2Uuid = 44444444-4444-4444-4444-444444444444`
   - `peca1Uuid = 55555555-5555-5555-5555-555555555555`
   - `peca2Uuid = 66666666-6666-6666-6666-666666666666`
6. Criar environment `Mekano Registry Local` com `baseUrl`.
7. Validar a coleção com o seed aplicado.
8. Documentar no README e em `docs/06-operacao-local.md`.

---

## 4. CI e cobertura

- Workflow: `.github/workflows/ci.yml`.
- Comando: `mvn -B verify`.
- PostgreSQL real via Dev Services/Testcontainers.
- JaCoCo configurado no POM pai.
- Cobertura mínima sugerida:
  - domínio e application: 80% de linhas;
  - infrastructure/rest: conforme viabilidade dos testes de integração.
- Falha de teste quebra o build.

---

## 5. Critérios de aceite da branch final

- [ ] Todos os testes migrados e verdes.
- [ ] Nenhum teste de domínio fora do escopo do Registry.
- [ ] Paths `/api/v4` em todos os testes REST.
- [ ] Postman do Registry executável com o seed.
- [ ] CI rodando a suíte completa.
- [ ] Cobertura configurada.
- [ ] Documentação atualizada.
- [ ] PR `test/sprint-5-testes-e-postman → main` mergeado após o Sprint 4.
