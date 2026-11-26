# Operação local — Mekano Registry Service API

**API:** `http://localhost:8081/api/v4`
**Banco:** `mekano_registry` em `localhost:5433`

---

## 1. Pré-requisitos

- Java 17
- Docker Desktop
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- DBeaver (opcional, para inspeção)
- Postman/Newman (opcional, para E2E)

---

## 2. Subir tudo com Docker Compose

```bash
docker compose up -d --build
```

Serviços:

| Serviço | Container | Porta |
|---|---|---|
| PostgreSQL | `registry-db` | `5433` |
| API | `registry-api` | `8081` |

Verificar:

```bash
docker compose ps
docker compose logs -f registry-api
```

---

## 3. Rodar apenas o banco e a API em modo dev

```bash
docker compose up -d registry-db
./mvnw quarkus:dev -pl registry-rest -am
```

Windows:

```powershell
docker compose up -d registry-db
.\mvnw.cmd quarkus:dev -pl registry-rest -am
```

---

## 4. Variáveis de ambiente

Crie `.env` a partir de `.env.example`:

```properties
REGISTRY_DB_NAME=mekano_registry
REGISTRY_DB_USER=mekano
REGISTRY_DB_PASSWORD=mekano
REGISTRY_DB_PORT=5433
REGISTRY_API_PORT=8081
```

No `application.properties`:

```properties
quarkus.rest.path=/api/v4
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5433/mekano_registry
quarkus.datasource.username=mekano
quarkus.datasource.password=mekano
quarkus.flyway.migrate-at-start=true
```

> As credenciais acima são **somente para desenvolvimento local**. Não usar em produção.

---

## 5. DBeaver

Após subir o container:

1. **Database → New Database Connection → PostgreSQL**.
2. Preencha:

| Campo | Valor |
|---|---|
| Host | `localhost` |
| Port | `5433` |
| Database | `mekano_registry` |
| Username | `mekano` |
| Password | `mekano` |

3. Clique em **Test Connection**.
4. Finalize e expanda `public` para ver as tabelas.

Tabelas esperadas após o seed:

- `clientes`
- `veiculos`
- `pecas`
- `servicos`
- `flyway_schema_history`

---

## 6. Endpoints úteis

| Recurso | URL |
|---|---|
| API | `http://localhost:8081/api/v4` |
| Swagger UI | `http://localhost:8081/q/swagger-ui` |
| OpenAPI | `http://localhost:8081/q/openapi` |
| Health | `http://localhost:8081/q/health` |
| Metrics | `http://localhost:8081/q/metrics` |

---

## 7. Seed e reset

O seed é aplicado pela migration `V5__seed_dados_iniciais.sql`.

Para resetar:

```bash
docker compose down -v
docker compose up -d --build
```

> `down -v` apaga o volume do PostgreSQL. Use apenas em desenvolvimento.

---

## 8. Postman

1. Importe `docs/postman/Mekano-Registry.postman_collection.json`.
2. Importe o environment `Mekano Registry Local`.
3. Confirme `baseUrl = http://localhost:8081/api/v4`.
4. Execute a coleção na ordem: clientes → veículos → peças → serviços → batch.

---

## 9. Troubleshooting

| Sintoma | Causa provável | Solução |
|---|---|---|
| `Connection refused` na API | Container não subiu | `docker compose logs registry-api` |
| `Flyway validation failed` | Migration alterada após aplicação | `docker compose down -v` e subir de novo |
| `Port 5433 already in use` | Outro PostgreSQL local | Alterar `REGISTRY_DB_PORT` ou parar o outro serviço |
| `Port 8081 already in use` | Outra API rodando | Alterar `REGISTRY_API_PORT` |
| DBeaver não conecta | Container parado ou senha errada | `docker compose ps` e conferir credenciais |
| Seed não aparece | Volume antigo | `docker compose down -v` e subir de novo |

---

## 10. Limpeza

```bash
docker compose down
docker compose down -v   # remove volumes
docker system prune -f   # opcional
```
