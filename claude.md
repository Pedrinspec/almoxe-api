# Almoxe — Contexto do projeto

Este arquivo é o briefing permanente do projeto. Ele consolida decisões, arquitetura e regras de negócio fechadas durante o planejamento. Leia-o antes de propor mudanças estruturais.

---

## Postura de trabalho (importante)

O dono do projeto é desenvolvedor Java experiente, mas **iniciante em React Native** e está usando o Claude Code para acelerar o desenvolvimento por restrição de tempo, **não para terceirizar o entendimento**. A regra de ouro é:

- **Modo "faça comigo", não "faça pra mim".** Antes de gerar arquivos novos ou refatorar algo não trivial, explique brevemente *o que* vai fazer e *por que*. Ao escrever código que envolva conceito novo (anotação JPA pouco usual, padrão React, hook específico, etc.), explique o conceito junto com a entrega — não despeje código sem contexto.
- **Conecte com Java quando explicar React/TypeScript.** O dono pensa em padrões Java; analogias ajudam muito.
- **Não invente regras de negócio.** Se algo não estiver descrito aqui ou no código, pergunte antes de assumir.
- **Decisões arquiteturais já tomadas não devem ser revisitadas** sem aviso explícito. Se você acha que algo aqui está errado, levante o ponto antes de mudar.

---

## Stack

**Backend (este repositório):**
- Java 25 (LTS, Temurin)
- Spring Boot 4.x
- Maven
- PostgreSQL 17 (rodando em contêiner Docker via `docker-compose.yml` na raiz)
- Lombok (com cuidado em Entities JPA — evitar `@Data`, preferir `@Getter`/`@Setter`/`@NoArgsConstructor`/`@AllArgsConstructor` separados)
- IDE: IntelliJ IDEA Ultimate
- Group: `com.almoxe`
- Artifact: `almoxe-api`
- Repositório: `almoxe-api` (separado do app)

**Frontend (repositório separado, futuro — `almoxe-app`):**
- React Native + Expo
- TypeScript
- Consumirá esta API via HTTPS

**Ambiente:**
- Desenvolvimento principal em macOS.
- Eventualmente Windows também — o projeto deve permanecer portável (atenção a CRLF/LF e caminhos).
- DBeaver como cliente visual do banco.

---

## Arquitetura do backend

**Padrão escolhido: arquitetura em camadas clássica do Spring** (não Clean Architecture). Foi uma decisão consciente — Clean Architecture foi considerada e descartada por custo de cerimônia desproporcional para um MVP feito por uma pessoa. Não migrar para Clean sem conversa explícita.

**Camadas (cada uma só chama a de baixo):**

1. **Filtro de segurança** (Spring Security) — ainda **não adicionado**. Será introduzido depois das primeiras funcionalidades estarem rodando. Não adicionar prematuramente.
2. **Controller** — recebe requisições HTTP, valida formato, delega ao Service, devolve JSON. Sem regra de negócio.
3. **Service** — cérebro. Toda regra de negócio mora aqui (transições de estado, validações de lote/NF/RI, alerta de estoque baixo, filtro por papel do usuário). Métodos que mexem em múltiplas tabelas devem ser `@Transactional`.
4. **Repository** — interfaces que estendem `JpaRepository`. Spring gera implementação.

**Entity vs DTO:**
- **Entity** = espelho da tabela do banco (anotada `@Entity`). Nunca exposta diretamente ao app.
- **DTO** = formato da API (JSON que vai/volta). Sem campos sensíveis (ex.: `senha_hash`). Conversão Entity↔DTO no Service.

**Organização de pastas: por funcionalidade**, não por camada técnica. Um pacote por área de domínio:
```
com.almoxe.almoxeapi
├── usuario/        (controller, service, repository, entity, dtos de usuário)
├── categoria/
├── produto/
├── item/           (ItemEstoque)
├── obra/
├── movimentacao/
├── foto/
├── config/         (configurações Spring)
└── common/         (tratamento de erros, utilitários compartilhados)
```

---

## Modelo de dados (DER)

Oito entidades principais. As três decisões de design por trás do modelo:

1. **Separação Produto / ItemEstoque** — Produto é o *catálogo* (definição: "Furadeira Bosch GSB 13 RE"). ItemEstoque é a *ocorrência física*. Um campo `tipo_controle` no Produto diz o comportamento: `UNIDADE_UNICA` (cada item é uma peça com identidade), `QUANTIDADE` (fungível, um único ItemEstoque com `quantidade` que sobe e desce), `LOTE` (rastreado por lote, com NF e RI). Os campos lote/NF/RI/número_serie no ItemEstoque ficam preenchidos ou vazios conforme o `tipo_controle`.

2. **Status como máquina de estados** — é enum fechado, transições validadas no Service. Veja a máquina de estados abaixo.

3. **Movimentação como entidade central** — registro imutável de evento. Material que volta ao estoque gera *outra* movimentação (tipo RETORNO), não edita a anterior. Histórico = lista de movimentações ordenadas por data. Status atual do item é consequência da última movimentação.

### Entidades

```
USUARIO
  id (uuid, PK)
  nome (string)
  email (string, unique)
  senha_hash (string)            -- nunca expor em DTO
  papel (enum: ALMOXARIFE, LIDER)

CATEGORIA
  id (uuid, PK)
  nome (string)

PRODUTO
  id (uuid, PK)
  categoria_id (FK)
  nome (string)
  descricao (string)
  tipo_controle (enum: UNIDADE_UNICA, QUANTIDADE, LOTE)
  estoque_minimo (int)           -- gatilho do alerta de estoque baixo

ITEM_ESTOQUE
  id (uuid, PK)
  produto_id (FK)
  responsavel_id (FK → USUARIO)  -- de quem é este material no momento
  status (enum: DISPONIVEL, ALOCADO, EM_USO, CONSUMIDO)
  quantidade (numeric)
  numero_serie (string, nullable)   -- preenchido se tipo_controle = UNIDADE_UNICA
  lote (string, nullable)           -- preenchidos se tipo_controle = LOTE
  nota_fiscal (string, nullable)
  numero_ri (string, nullable)

OBRA
  id (uuid, PK)
  nome (string)
  endereco (string)
  ativa (boolean)                -- soft delete (obras antigas continuam referenciadas)

MOVIMENTACAO
  id (uuid, PK)
  item_estoque_id (FK)
  obra_id (FK)
  usuario_id (FK)                -- quem registrou / responsável pela movimentação
  tipo (enum: ENTRADA, ALOCACAO, RETORNO, BAIXA)
  quantidade (numeric)
  data_hora (timestamp)
  observacao (string, nullable)
  -- registros imutáveis; nunca editar/deletar

FOTO
  id (uuid, PK)
  movimentacao_id (FK)
  caminho_arquivo (string)       -- só o caminho; arquivo fica fora do banco
  data_upload (timestamp)
```

### Relacionamentos
- USUARIO 1—* MOVIMENTACAO (quem registra)
- USUARIO 1—* ITEM_ESTOQUE (responsável atual)
- CATEGORIA 1—* PRODUTO
- PRODUTO 1—* ITEM_ESTOQUE
- ITEM_ESTOQUE 1—* MOVIMENTACAO
- OBRA 1—* MOVIMENTACAO
- MOVIMENTACAO 1—* FOTO

---

## Máquina de estados do ItemEstoque

Quatro estados:
- **DISPONIVEL** — no almoxarifado, pronto para uso.
- **ALOCADO** — reservado para uma obra, equipe ainda não começou a usar. Este estado existe porque o cliente confirmou que há intervalo entre separar e usar.
- **EM_USO** — equipe está com o item em campo. Indisponível para outras equipes.
- **CONSUMIDO** — baixa definitiva, fim do ciclo.

Transições permitidas:
- DISPONIVEL → ALOCADO  (gera MOVIMENTACAO tipo ALOCACAO)
- ALOCADO → EM_USO       (gera MOVIMENTACAO tipo ALOCACAO ou subtipo equivalente)
- ALOCADO → DISPONIVEL   (gera MOVIMENTACAO tipo RETORNO — equipe desistiu antes de usar)
- EM_USO → DISPONIVEL    (gera MOVIMENTACAO tipo RETORNO)
- EM_USO → CONSUMIDO     (gera MOVIMENTACAO tipo BAIXA)
- (entrada de material novo no almoxarifado) → DISPONIVEL (gera MOVIMENTACAO tipo ENTRADA)

Qualquer outra transição é inválida e deve ser rejeitada no Service.

**Para itens com `tipo_controle = QUANTIDADE`** (fungíveis tipo parafusos), a máquina de estados se aplica de forma mais branda: o ItemEstoque não muda de status, só a `quantidade` diminui em uma BAIXA parcial. Isso é intencional, reflete como material fungível funciona na vida real.

---

## Regras de negócio

- **Alerta de estoque baixo:** quando a soma das `quantidade` disponíveis de um Produto fica abaixo do `estoque_minimo` dele, dispara alerta. (Forma do alerta — push, e-mail, badge na tela — ainda não definida.)

- **Lote/NF/RI obrigatórios:** quando `Produto.tipo_controle = LOTE`, criar um ItemEstoque sem `lote`, `nota_fiscal` e `numero_ri` deve falhar com erro de validação no Service.

- **Permissão por papel (CRÍTICO — implementar no backend, não só no app):**
    - `ALMOXARIFE`: vê o estoque inteiro, cadastra produtos e dá entrada de material.
    - `LIDER`: vê apenas itens onde ele é o `responsavel_id`. Quando consulta a API, o Service filtra automaticamente por usuário autenticado se papel = LIDER.
    - **Nunca depender só do app esconder o botão.** O backend é a fonte de verdade da permissão.

- **Senhas:** sempre armazenadas como hash (Spring Security cuidará disso). Nunca em texto puro, nem mesmo em desenvolvimento.

---

## Fluxo de foto

Decisão: o app **não** integra câmera nativa. Em vez disso:
1. App abre via deep link um app externo de timestamp camera (ex.: Timestamp Camera Free) — usuário tira a foto lá, com geolocalização + data queimadas na imagem.
2. Foto fica na galeria do celular.
3. No app Almoxe, usuário **anexa manualmente** uma ou mais fotos da galeria a uma Movimentação (upload).
4. Backend recebe a foto via endpoint dedicado, salva o arquivo em armazenamento de arquivos (no MVP: pasta local; em cloud: object storage tipo S3), e grava apenas o caminho na tabela FOTO.

Arquivos nunca vão dentro do Postgres como BLOB.

---

## Decisões adiadas (não implementar agora, mas manter porta aberta)

- **Spring Security / autenticação:** entra depois das primeiras funcionalidades CRUD estarem rodando.
- **Offline-first no app:** descartado para o MVP porque o cliente confirmou que líderes têm 5G. Manter no app uma camada de Repository isolada para que adicionar offline depois não exija refactor.
- **Versão web (responsividade desktop):** não no MVP. Construir telas com layout flexível desde já para não bloquear essa porta.
- **Categoria opcional:** entra no MVP (decisão tomada).
- **Subir para cloud:** depois do MVP rodando local. A arquitetura local já é a mesma da cloud — só muda hospedagem do backend (contêiner em servidor) e do banco (Postgres gerenciado), e a URL base no app. Object storage para fotos quando migrar.

---

## Ambiente local

**Banco rodando via Docker Compose** (arquivo `docker-compose.yml` na raiz do projeto):
- Imagem: `postgres:17`
- Banco: `estoque`
- Usuário: `estoque_app`
- Senha: `senha_local_dev` (fraca de propósito — só ambiente local; trocar antes de cloud)
- Porta: `5432:5432`
- Volume persistente: `dados_postgres`

Comandos:
- Subir: `docker compose up -d`
- Parar: `docker compose stop`
- Religar: `docker compose start`
- Status: `docker ps`

---

## Estado atual do projeto

- Setup de ambiente: **completo** (Java 25, Docker, Postgres rodando, DBeaver conectado, projeto Spring Boot gerado e aberto no IntelliJ).
- Dependências no `pom.xml`: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Boot DevTools, Lombok.
- Conexão `application.properties` com o banco: **pendente** — próximo passo imediato.
- Versionamento Git/GitHub: **pendente** — fazer logo após o primeiro `application.properties` funcionar. Usar "Share Project on GitHub" do IntelliJ Ultimate. Repositório `almoxe-api`.
- Primeira entidade: **pendente** — sugestão de começar por uma simples (Categoria ou Obra) para validar o ciclo completo Entity → Repository → Service → Controller antes de enfrentar ItemEstoque com a máquina de estados.
- Nenhum código de domínio escrito ainda.

---

## Próximos marcos sugeridos (na ordem)

1. Configurar `application.properties` com a conexão ao Postgres do Docker.
2. Subir o backend pela primeira vez — confirmar "Started AlmoxeApiApplication" no log. Apenas o esqueleto, sem entidades ainda.
3. Inicializar Git e versionar no GitHub (`almoxe-api`).
4. Primeira entidade simples (Categoria ou Obra): Entity + Repository + Service + Controller + DTO + um endpoint CRUD funcional, testado via Postman/curl ou DBeaver.
5. Segunda entidade (Produto), introduzindo o conceito de `tipo_controle`.
6. ItemEstoque + máquina de estados — a parte mais densa do domínio.
7. Movimentação e o fluxo de uso/retorno/baixa.
8. Upload de foto.
9. Spring Security + papéis.
10. Aplicação cliente (repositório separado `almoxe-app`).

Não avançar para o passo seguinte sem o anterior funcionar de ponta a ponta.
