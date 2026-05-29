# Almoxe API

API REST para controle de almoxarifado e movimentação de materiais de obra: catálogo de produtos, controle de estoque com ciclo de vida do item (entrada → alocação → uso → devolução/baixa), obras, histórico de movimentações, fotos e alerta de estoque baixo. Autenticação via JWT com autorização por papel.

> Briefing completo de domínio e decisões de arquitetura em [CLAUDE.md](CLAUDE.md).

---

## Stack

- **Java 25** (Temurin)
- **Spring Boot 4.0.x** (Spring Framework 7, Spring Security 7)
- **Maven** (wrapper `./mvnw`)
- **PostgreSQL 17** (via Docker Compose)
- **Flyway** — versionamento de schema
- **springdoc-openapi 3.0.3** — Swagger/OpenAPI 3.1
- **jjwt 0.12.6** — emissão/validação de JWT
- **Lombok** — boilerplate (getters/setters, `@Slf4j`)

---

## Arquitetura

Arquitetura em **camadas clássica do Spring** (Controller → Service → Repository), com **organização por funcionalidade** (um pacote por área de domínio), não por camada técnica.

- **Controller** — recebe HTTP, valida formato (`@Valid`), delega ao Service. Sem regra de negócio.
- **Service** — toda a regra de negócio (transições de estado, validações por tipo de controle, ownership, etc.). Métodos multi-tabela são `@Transactional`.
- **Repository** — interfaces `JpaRepository`.
- **Entity vs DTO** — entidades nunca são expostas; conversão Entity↔DTO (records) no Service. Campos sensíveis (ex.: `senhaHash`) ficam fora dos DTOs.

### Esquema de pastas

```
src/main/java/com/almoxe/almoxeapi/
├── AlmoxeApiApplication.java        # entrypoint Spring Boot
├── usuario/                         # Usuario, Papel, cadastro e consulta
├── categoria/                       # CRUD de Categoria
├── produto/                         # CRUD de Produto + alerta de estoque baixo
├── item/                            # ItemEstoque, StatusItem, ENTRADA e máquina de estados
├── movimentacao/                    # Movimentacao (registro imutável), TipoMovimentacao
├── obra/                            # CRUD de Obra (soft delete)
├── foto/                            # upload/download de fotos + armazenamento em disco
├── security/                        # JWT, filtro, SecurityConfig, login
├── config/                          # OpenAPI (Swagger) e Jackson
└── common/                          # ErrorResponse, exceções, handler global, filtro de log

src/main/resources/
├── application.properties
└── db/migration/                    # V1..V7 (Flyway)
```

### Modelo de dados

```
USUARIO 1—* MOVIMENTACAO            (quem registrou)
USUARIO 1—* ITEM_ESTOQUE            (responsável atual)
CATEGORIA 1—* PRODUTO               (categoria é opcional)
PRODUTO 1—* ITEM_ESTOQUE
ITEM_ESTOQUE 1—* MOVIMENTACAO
OBRA 1—* MOVIMENTACAO
MOVIMENTACAO 1—* FOTO
```

Migrations: `V1` categoria · `V2` produto · `V3` usuario (+ seed) · `V4` item_estoque · `V5` movimentacao · `V6` obra (+ FK em movimentacao) · `V7` foto.

---

## Como rodar localmente

### Pré-requisitos
- Java 25 (JDK)
- Docker + Docker Compose
- (opcional) DBeaver para inspecionar o banco

### 1. Subir o PostgreSQL
```bash
docker compose up -d
```
Sobe o container `almoxe-postgres` (banco `almoxe_db`, usuário `almoxe_app`, senha `senha_local_dev`, porta `5432`). Comandos úteis: `docker compose stop` / `start` / `down -v` (este último apaga o volume).

### 2. Subir a aplicação
```bash
./mvnw spring-boot:run
```
Aguarde `Started AlmoxeApiApplication`. A API sobe em `http://localhost:8080`. O Flyway aplica as migrations automaticamente no boot.

### 3. Rodar os testes
```bash
./mvnw test
```

### Swagger / OpenAPI
- UI interativa: `http://localhost:8080/swagger-ui.html`
- Spec JSON: `http://localhost:8080/v3/api-docs`

### Coleção Postman
Importe [almoxe-api.postman_collection.json](almoxe-api.postman_collection.json) no Postman. Rode **Autenticação → Login (almoxarife)** primeiro — o token é salvo na variável `{{token}}` e usado como Bearer em toda a coleção.

---

## Autenticação e autorização

Autenticação **JWT stateless**: faça login, receba um token e envie-o em `Authorization: Bearer <token>` nas demais requisições.

```bash
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"almoxarife@almoxe.dev","senha":"senha123"}'
```

### Usuários de desenvolvimento (seed)
| Email | Senha | Papel |
|-------|-------|-------|
| `almoxarife@almoxe.dev` | `senha123` | ALMOXARIFE |
| `lider@almoxe.dev` | `senha123` | LIDER |

### Papéis
- **ALMOXARIFE** — gere o estoque inteiro: cadastra catálogo (categoria/produto/obra/usuário), dá entrada de material, aloca itens, vê tudo.
- **LIDER** — vê e opera **apenas os itens onde é o responsável** (filtro aplicado no backend, não só no app). Usa/retorna/baixa os itens recebidos.

---

## Endpoints

Legenda de acesso: **público** · **auth** (qualquer autenticado) · **ALMOXARIFE** (só esse papel).

### Autenticação
| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/auth/login` | público | Autentica e retorna JWT |

### Usuários
| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/usuarios` | ALMOXARIFE | Cadastra usuário (senha com hash BCrypt) |
| GET | `/usuarios` | auth | Lista usuários |
| GET | `/usuarios/{id}` | auth | Busca usuário |

### Categorias
| Método | Rota | Acesso |
|--------|------|--------|
| POST | `/categorias` | ALMOXARIFE |
| GET | `/categorias` · `/categorias/{id}` | auth |
| PUT | `/categorias/{id}` | ALMOXARIFE |
| DELETE | `/categorias/{id}` | ALMOXARIFE |

### Produtos
| Método | Rota | Acesso |
|--------|------|--------|
| POST | `/produtos` | ALMOXARIFE |
| GET | `/produtos` · `/produtos/{id}` | auth |
| PUT | `/produtos/{id}` | ALMOXARIFE |
| DELETE | `/produtos/{id}` | ALMOXARIFE |
| GET | `/produtos/estoque-baixo` | ALMOXARIFE |

### Obras
| Método | Rota | Acesso |
|--------|------|--------|
| POST | `/obras` | ALMOXARIFE |
| GET | `/obras` · `/obras/{id}` | auth |
| PUT | `/obras/{id}` | ALMOXARIFE |
| DELETE | `/obras/{id}` | ALMOXARIFE (soft delete: `ativa=false`) |

### Estoque e movimentações
| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/itens/entrada` | ALMOXARIFE | Entrada de material (cria/soma item DISPONIVEL + movimentação ENTRADA) |
| GET | `/itens` | auth | Lista itens (LIDER vê só os seus) |
| GET | `/itens/{id}` | auth | Busca item (LIDER só os seus) |
| GET | `/itens/{id}/movimentacoes` | auth | Histórico de movimentações do item |
| POST | `/itens/{id}/alocacao` | ALMOXARIFE | DISPONIVEL → ALOCADO (define obra + responsável) |
| POST | `/itens/{id}/uso` | auth | ALOCADO → EM_USO |
| POST | `/itens/{id}/retorno` | auth | ALOCADO/EM_USO → DISPONIVEL |
| POST | `/itens/{id}/baixa` | auth | EM_USO → CONSUMIDO (ou baixa parcial p/ fungível) |

### Fotos
| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/movimentacoes/{id}/fotos` | auth | Upload (multipart, 1+ imagens jpg/png/webp, até 10MB cada) |
| GET | `/movimentacoes/{id}/fotos` | auth | Lista fotos da movimentação |
| GET | `/fotos/{id}` | auth | Baixa/exibe o arquivo da foto |

---

## Regras de negócio

### Tipo de controle do Produto
O campo `tipoControle` define como o material é rastreado e como a entrada/baixa se comportam:

- **UNIDADE_UNICA** — peça com identidade própria (ex.: furadeira). Cada entrada exige `numeroSerie` e quantidade **1**. Percorre a máquina de estados inteira.
- **QUANTIDADE** — fungível (ex.: parafusos). Mantém **um único** ItemEstoque por produto: a entrada **soma** na quantidade existente; o status permanece DISPONIVEL e a **baixa parcial** reduz a quantidade.
- **LOTE** — rastreado por lote (ex.: cimento). Cada entrada exige `lote`, `notaFiscal` e `numeroRi`. Percorre a máquina de estados inteira.

### Máquina de estados do ItemEstoque (UNIDADE_UNICA e LOTE)
```
                 ENTRADA
                    │
                    ▼
              DISPONIVEL ◄────────── RETORNO ──────────┐
                    │                                  │
              ALOCACAO                                 │
                    ▼                                  │
                ALOCADO ──── RETORNO ──► DISPONIVEL    │
                    │                                  │
                  (uso)                                │
                    ▼                                  │
                 EM_USO ───────────────────────────────┘
                    │
                  BAIXA
                    ▼
               CONSUMIDO
```
Transições inválidas são rejeitadas no Service (HTTP 422). Para **QUANTIDADE** o status não muda — só a baixa reduz a quantidade.

### Outras regras
- **Alocação** exige obra **ativa** e define o `responsavel` (líder). Só ALMOXARIFE aloca. Itens QUANTIDADE não são alocados (use baixa).
- **Retorno** zera o responsável (item volta ao almoxarifado).
- **Ownership do LIDER** — o líder só enxerga/opera itens onde é responsável; acesso a item de outro retorna 404 (não revela existência).
- **Alerta de estoque baixo** — soma das quantidades DISPONIVEL de um produto abaixo do `estoqueMinimo`; inclui produtos sem nenhum item (soma zero).
- **Movimentação é imutável** — nunca editada/removida; o histórico é a lista ordenada por data.
- **Fotos** — arquivo salvo em disco (`./uploads/fotos`, configurável); o banco guarda só o caminho. Nome gerado por UUID no servidor.

---

## Tratamento de erros

Respostas de erro têm formato uniforme:
```json
{
  "timestamp": "2026-05-28T03:00:00Z",
  "status": 422,
  "title": "Regra de negócio violada",
  "details": ["Produto com controle por LOTE exige lote, nota fiscal e número de RI."]
}
```

| Status | Quando |
|--------|--------|
| 400 | Corpo/parâmetro inválido (validação `@Valid`, JSON malformado, tipo incompatível) |
| 401 | Não autenticado (token ausente/inválido) ou credenciais inválidas no login |
| 403 | Autenticado, mas o papel não tem permissão |
| 404 | Recurso não encontrado (ou item fora do escopo do LIDER) |
| 409 | Conflito de integridade (ex.: nome de categoria duplicado, FK em uso) |
| 413 | Upload acima do limite (10MB/arquivo) |
| 422 | Regra de negócio violada (transição inválida, campos obrigatórios por tipo, etc.) |
| 500 | Erro inesperado (logado com stacktrace; resposta sem detalhes internos) |

---

## Logging

- `@Slf4j` (Lombok) nos Services, logando eventos de negócio em **INFO** (entrada, alocação, uso, retorno, baixa, login, cadastros). Nunca loga senha/token/hash.
- `RequestLogFilter` loga cada requisição: `MÉTODO URI -> STATUS (Xms)` — inclusive respostas 401/403.
- Violações de regra em **WARN**; erros inesperados em **ERROR** (com stacktrace).
- Nível configurável em `application.properties`: `logging.level.com.almoxe=INFO`.

---

## Roadmap (futuro)

- Suíte de testes unitários e de integração.
- Refresh token, logout, troca/recuperação de senha.
- Rate limiting no login.
- Deploy em cloud: HTTPS/TLS, PostgreSQL gerenciado, object storage (S3) para as fotos, secret do JWT via variável de ambiente.
- App cliente (repositório `almoxe-app`, React Native + Expo).
