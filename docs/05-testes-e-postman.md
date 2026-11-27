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

Os artefatos base do Sprint 3 estão em:

- `docs/postman/Mekano-Registry.postman_collection.json`
- `docs/postman/Mekano-Registry-Local.postman_environment.json`

Importe ambos no Postman e selecione o environment **Mekano Registry Local**. A coleção usa `baseUrl` e UUIDs fixos do seed e contém consultas de leitura e a chamada do batch interno para evitar alterar os dados seeded.

As requisições de criação/alteração e a migração integral dos cenários Newman da coleção original permanecem no escopo da branch final `test/sprint-5-testes-e-postman`. A coleção base do Sprint 3 não contém operações destrutivas.

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
