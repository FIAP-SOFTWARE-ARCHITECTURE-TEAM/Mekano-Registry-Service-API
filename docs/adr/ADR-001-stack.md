# ADR-001 — Stack Java 17 + Quarkus 3.36.0 + Maven multi-módulo

**Status:** Aceita
**Fase:** 4
**Data:** 2026-11-26

---

## 1. Contexto

O Registry é um serviço novo, mas faz parte do ecossistema Mekano, cujo monólito já utiliza Java 17, Quarkus 3.36.0, Maven Wrapper, Lombok, MapStruct, Jandex, Hibernate ORM/Panache, Flyway e PostgreSQL 16.

A equipe precisa entregar o serviço em sprints curtas, com um padrão já conhecido, reaproveitando decisões e práticas do monólito. Ao mesmo tempo, o serviço deve ser independente, com banco próprio e deploy separado.

---

## 2. Decisão

Adotar o mesmo stack do monólito:

| Item | Versão/uso |
|---|---|
| Java | 17 (`maven.compiler.release=17`) |
| Quarkus | 3.36.0 |
| Maven | Multi-módulo com wrapper (`mvnw` / `mvnw.cmd`) |
| Lombok | Redução de boilerplate |
| MapStruct | `componentModel = "cdi"` |
| Jandex | Índice nos módulos `domain`, `application`, `infrastructure` e `rest` |
| Hibernate ORM / Panache | Persistência |
| Flyway | Migrations |
| PostgreSQL | 16 (ver ADR-002) |
| Testes | JUnit 5, AssertJ, Mockito, RestAssured, H2 em memória |

### Estrutura Maven

```text
mekano-registry (pom)
├── registry-domain
├── registry-application
├── registry-infrastructure
└── registry-rest (packaging=quarkus)
```

Regras de build:

1. `quarkus-maven-plugin` apenas no `registry-rest`.
2. `jandex-maven-plugin` nos módulos indexáveis.
3. Ordem dos annotation processors: Lombok → `lombok-mapstruct-binding` → MapStruct.
4. `@Mapper(componentModel = "cdi")` em todos os mappers MapStruct.
5. `mvn -B verify` como comando único de validação local e no CI.

---

## 3. Consequências

### Positivas

1. **Curva de aprendizado zero** para a equipe que já mantém o monólito.
2. **Startup rápido e baixo consumo** em relação a stacks tradicionais.
3. **Clean Architecture explícita** em módulos Maven, com dependências unidirecionais.
4. **Build reprodutível** com Maven Wrapper no CI e no Docker.
5. **Testes em camadas**: domínio puro, application com Mockito, infraestrutura/REST com `@QuarkusTest`.

### Negativas / custos

1. **Duplicação de convenções** entre monólito e Registry (build, mappers, DTOs).
2. **Configuração de annotation processors** é sensível à ordem e ao módulo.
3. **Quarkus Maven Plugin fora do `registry-rest`** quebra o build; a regra precisa ser respeitada.
4. **H2 em testes** não cobre todos os recursos do PostgreSQL (ex.: índices parciais).

### Mitigações

- Documentação das regras em `docs/01-arquitetura.md` e neste ADR.
- CI executando `mvn -B verify` em todos os PRs.
- Testes de integração com H2 no modo PostgreSQL e validação das migrations em ambiente Docker/PostgreSQL no PR.

---

## 4. Alternativas consideradas

### Alternativa A — Spring Boot (rejeitada)

- Stack madura, mas diferente do monólito.
- Exigiria reescrever convenções, build e padrões de teste.
- **Motivo da rejeição:** custo de padronização e inconsistência com o restante do projeto.

### Alternativa B — Módulo único (rejeitada)

- Um único projeto Maven com pacotes por camada.
- Mais simples no início, porém não impede dependências invertidas.
- **Motivo da rejeição:** o objetivo didático e arquitetural é explicitar as fronteiras entre domínio, aplicação, infraestrutura e REST.

### Alternativa C — Quarkus com Java 21 (rejeitada nesta fase)

- Java 21 é LTS e suportado pelo Quarkus 3.36.
- O monólito e o ambiente de execução padronizam Java 17.
- **Motivo da rejeição:** manter paridade com o monólito e com a imagem base `eclipse-temurin:17`.

---

## 5. Referências

- [`../01-arquitetura.md`](../01-arquitetura.md)
- [`ADR-002-postgresql.md`](ADR-002-postgresql.md)
- [`ADR-004-comunicacao-rest.md`](ADR-004-comunicacao-rest.md)
- [`ADR-005-migrations-finais.md`](ADR-005-migrations-finais.md)
